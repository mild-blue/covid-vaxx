package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dto.request.VaccinationDtoIn
import blue.mild.covid.vaxx.dto.request.query.PatientIdQueryDtoIn
import blue.mild.covid.vaxx.dto.request.query.VaccinationIdDtoIn
import blue.mild.covid.vaxx.dto.response.VaccinationDetailDtoOut
import blue.mild.covid.vaxx.dto.response.toPatientVaccinationDetailDto
import blue.mild.covid.vaxx.extensions.asContextAware
import blue.mild.covid.vaxx.extensions.closestDI
import blue.mild.covid.vaxx.extensions.createLogger
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.service.IsinServiceInterface
import blue.mild.covid.vaxx.service.PatientService
import blue.mild.covid.vaxx.service.VaccinationService
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.get
import com.papsign.ktor.openapigen.route.path.auth.post
import com.papsign.ktor.openapigen.route.path.auth.principal
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.kodein.di.instance

/**
 * Registers routes related to the vaccinations.
 */
fun NormalOpenAPIRoute.vaccinationRoutes() {
    val logger = createLogger("VaccinationRoutes")

    val vaccinationService by closestDI().instance<VaccinationService>()
    val patientService by closestDI().instance<PatientService>()
    val isinService by closestDI().instance<IsinServiceInterface>()

    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN, UserRole.DOCTOR)) {
        route(Routes.vaccination) {
            get<VaccinationIdDtoIn, VaccinationDetailDtoOut, UserPrincipal>(
                info("Get vaccination detail by given vaccination id.")
            ) { (vaccinationId) ->
                val userId = principal().userId
                logger.info { "User $userId requested data about vaccination ID $vaccinationId." }
                respond(vaccinationService.get(vaccinationId))
            }

            // The following endpoint gets first dose only, which is currently ok because we do not use it
            get<PatientIdQueryDtoIn, VaccinationDetailDtoOut, UserPrincipal>(
                info("Get first dose vaccination detail for given patient ID.")
            ) { (patientId) ->
                val userId = principal().userId
                logger.info { "User $userId requested vaccination data about patient with ID $patientId." }
                respond(vaccinationService.getForPatient(patientId, 1))
            }

            post<Unit, VaccinationDetailDtoOut, VaccinationDtoIn, UserPrincipal>(
                info("Register that the patient was vaccinated. Returns vaccination detail.")
            ) { _, request ->
                if (request.doseNumber != 1 && request.doseNumber != 2) {
                    throw IllegalArgumentException("Vaccination creation failed because doseNumber was set to ${request.doseNumber}")
                }

                val principal = principal()
                logger.info { "User ${principal.userId} vaccinated patient ${request.patientId} with dose number ${request.doseNumber}." }

                val vaccinationId = vaccinationService.addVaccination(asContextAware(request))

                logger.info { "Vaccination was successful, exporting to ISIN." }

                val vaccination = vaccinationService.get(vaccinationId)
                val patient = patientService.getPatientById(request.patientId)

                // Try to export vaccination to ISIN
                val wasExportedToIsin = isinService.tryCreateVaccination(
                    vaccination.toPatientVaccinationDetailDto(),
                    patient = patient
                )
                if (wasExportedToIsin)
                    vaccinationService.exportedToIsin(vaccinationId)

                logger.info { "Job sent successfully." }

                respond(vaccination)
            }
        }
    }
}
