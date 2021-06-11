package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.DatabaseSetup
import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dao.repository.PatientRepository
import blue.mild.covid.vaxx.dto.internal.PatientValidationResult
import blue.mild.covid.vaxx.dto.request.IsinJobDtoIn
import blue.mild.covid.vaxx.dto.request.query.SystemStatisticsFilterDtoIn
import blue.mild.covid.vaxx.dto.response.ApplicationInformationDtoOut
import blue.mild.covid.vaxx.dto.response.IsinJobDtoOut
import blue.mild.covid.vaxx.dto.response.ServiceHealthDtoOut
import blue.mild.covid.vaxx.dto.response.SystemStatisticsDtoOut
import blue.mild.covid.vaxx.dto.response.toPatientVaccinationDetailDto
import blue.mild.covid.vaxx.extensions.closestDI
import blue.mild.covid.vaxx.extensions.createLogger
import blue.mild.covid.vaxx.extensions.determineRealIp
import blue.mild.covid.vaxx.extensions.request
import blue.mild.covid.vaxx.extensions.respondWithStatus
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.service.DataCorrectnessService
import blue.mild.covid.vaxx.service.IsinServiceInterface
import blue.mild.covid.vaxx.service.PatientService
import blue.mild.covid.vaxx.service.PatientValidationService
import blue.mild.covid.vaxx.service.SystemStatisticsService
import blue.mild.covid.vaxx.service.VaccinationService
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.get
import com.papsign.ktor.openapigen.route.path.auth.post
import com.papsign.ktor.openapigen.route.path.auth.principal
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import org.kodein.di.instance

/**
 * Registers prometheus data.
 */
fun NormalOpenAPIRoute.serviceRoutes() {
    val version by closestDI().instance<ApplicationInformationDtoOut>()
    val systemStatisticsService by closestDI().instance<SystemStatisticsService>()
    val patientService by closestDI().instance<PatientService>()
    val patientValidation by closestDI().instance<PatientValidationService>()
    val isinService by closestDI().instance<IsinServiceInterface>()
    val dataCorrectnessService by closestDI().instance<DataCorrectnessService>()
    val patientRepository by closestDI().instance<PatientRepository>()
    val vaccinationService by closestDI().instance<VaccinationService>()

    val logger = createLogger("ServiceRoute")

    /**
     * Send data about version.
     */
    route(Routes.version).get<Unit, ApplicationInformationDtoOut>(
        info("Returns version of the application.")
    ) { respond(version) }

    route(Routes.status).get<Unit, Unit> { respondWithStatus(HttpStatusCode.OK) }

    route(Routes.statusHealth).get<Unit, ServiceHealthDtoOut> {
        if (DatabaseSetup.isConnected()) {
            respond(ServiceHealthDtoOut("healthy"))
        } else {
            request.call.respond(HttpStatusCode.ServiceUnavailable, ServiceHealthDtoOut("DB connection is not working"))
        }
    }

    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN)) {
        route(Routes.systemStatistics).get<SystemStatisticsFilterDtoIn, SystemStatisticsDtoOut, UserPrincipal> { query ->
            respond(systemStatisticsService.getSystemStatistics(query))
        }
    }

    // TODO move functionality somewhere else
    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN)) {
        route(Routes.runIsinJob).post<Unit, IsinJobDtoOut, IsinJobDtoIn, UserPrincipal>(
            info(
                "Checks patients where ISIN was failed and run isin client again."
            )
        ) { _, isinJobDto ->
            val principal = principal()
            logger.info {
                "Run ISIN job by ${principal.userId} from host ${request.determineRealIp()}."
            }

            val stats = IsinJobDtoOut()

            val patients = patientService.getPatientsByConjunctionOf(
                n = isinJobDto.patientsCount,
                offset = isinJobDto.patientsOffset.toLong()
            )

            for (patient in patients) {
                logger.debug("Checking ISIN id of patient ${patient.id}")

                // 1. If patient has personal number but ISIN id is not set -> try ISIN validation
                val isinId = if (!patient.isinId.isNullOrBlank()) {
                    patient.isinId
                } else if (isinJobDto.validatePatients && !patient.personalNumber.isNullOrBlank() ) {
                    logger.info("Patient ${patient.id} has personal number but no ISIN id. Validating in ISIN...")

                    val patientValidationResult = patientValidation.validatePatient(
                        firstName = patient.firstName,
                        lastName = patient.lastName,
                        personalNumber = patient.personalNumber,
                    )

                    logger.info {
                        "Validation of patient ${patient.firstName}/${patient.lastName}/${patient.personalNumber} " +
                        "completed: status=${patientValidationResult.status}, isinPatientId=${patientValidationResult.patientId}."
                    }

                    val newIsinPatientId = when (patientValidationResult.status) {
                        PatientValidationResult.PATIENT_FOUND ->
                            patientValidationResult.patientId
                        else -> {
                            null
                        }
                    }

                    if (newIsinPatientId != null) {
                        logger.debug { "Updating ISIN id of patient ${patient.id} to $newIsinPatientId"}
                        patientRepository.updatePatientChangeSet(id = patient.id, isinId = newIsinPatientId.trim())
                        stats.validatedPatientsSuccess++;
                    } else {
                        logger.debug { "NOT updating ISIN id of patient ${patient.id}"}
                        stats.validatedPatientsErrors++;
                    }
                    newIsinPatientId
                } else {
                    null
                }

                if (isinId.isNullOrBlank()) continue

                // 2. If data are correct but not exported to ISIN -> try export to isin
                logger.debug("Checking correctness exported to ISIN of patient ${patient.id}")
                if (isinJobDto.exportPatientsInfo && patient.dataCorrect != null && patient.dataCorrect.dataAreCorrect && patient.dataCorrect.exportedToIsinOn == null) {
                    val wasExported = isinService.tryExportPatientContactInfo(patient, notes= patient.dataCorrect.notes)

                    if (wasExported) {
                        logger.debug { "Updating exported to ISIN of correctness ${patient.dataCorrect.id} (patient ${patient.id})"}
                        dataCorrectnessService.exportedToIsin(patient.dataCorrect.id)
                        stats.exportedPatientsInfoSuccess++
                    } else {
                        logger.debug { "NOT updating exported to ISIN of correctness ${patient.dataCorrect.id} (patient ${patient.id})"}
                        stats.exportedPatientsInfoErrors++
                    }
                }

                // 3. If vaccinated but not vaccination is not exported to ISIN -> try export vaccination to isin
                if (isinJobDto.exportVaccinations && patient.vaccinated != null && patient.vaccinated.exportedToIsinOn == null) {
                    val vaccination = vaccinationService.get(patient.vaccinated.id)

                    // Try to export vaccination to ISIN
                    val wasExported = isinService.tryCreateVaccinationAndDose(
                        vaccination.toPatientVaccinationDetailDto(),
                        patient = patient
                    )

                    if (wasExported) {
                        logger.debug { "Updating exported to ISIN of vaccination ${patient.vaccinated.id} (patient ${patient.id})"}
                        vaccinationService.exportedToIsin(patient.vaccinated.id)
                        stats.exportedVaccinationsSuccess++
                    } else {
                        logger.debug { "NOT updating exported to ISIN of vaccination ${patient.vaccinated.id} (patient ${patient.id})"}
                        stats.exportedVaccinationsErrors++
                    }
                }
            }

            respond(stats)
        }
    }
}
