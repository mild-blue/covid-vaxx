package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dto.request.VaccinationDtoIn
import blue.mild.covid.vaxx.dto.request.query.PatientIdQueryDtoIn
import blue.mild.covid.vaxx.dto.request.query.VaccinationIdDtoIn
import blue.mild.covid.vaxx.dto.response.VaccinationDetailDtoOut
import blue.mild.covid.vaxx.dto.response.toPatientVaccinationDetailDto
import blue.mild.covid.vaxx.extensions.asContextAware
import blue.mild.covid.vaxx.extensions.closestDI
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.service.MedicalRegistrationService
import blue.mild.covid.vaxx.service.VaccinationService
import blue.mild.covid.vaxx.utils.createLogger
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
    val medicalRegistrationService by closestDI().instance<MedicalRegistrationService>()

    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN, UserRole.DOCTOR)) {
        route(Routes.vaccination) {
            get<VaccinationIdDtoIn, VaccinationDetailDtoOut, UserPrincipal>(
                info("Get vaccination detail by given vaccination id.")
            ) { (vaccinationId) ->
                respond(vaccinationService.get(vaccinationId))
            }

            get<PatientIdQueryDtoIn, VaccinationDetailDtoOut, UserPrincipal>(
                info("Get vaccination detail for given patient ID.")
            ) { (patientId) ->
                respond(vaccinationService.getForPatient(patientId))
            }

            post<Unit, VaccinationDetailDtoOut, VaccinationDtoIn, UserPrincipal>(
                info("Register that the patient was vaccinated. Returns vaccination detail.")
            ) { _, request ->
                val principal = principal()
                logger.debug { "User ${principal.userId} vaccinated patient ${request.patientId}." }

                val vaccinationId = vaccinationService.addVaccination(asContextAware(request))
                val vaccination = vaccinationService.get(vaccinationId)

                logger.debug { "Vaccination was successful, registering in the medical system." }
                medicalRegistrationService.registerPatientsVaccination(vaccination.toPatientVaccinationDetailDto())
                logger.debug { "Job sent successfully." }

                respond(vaccination)
            }
        }
    }
}
