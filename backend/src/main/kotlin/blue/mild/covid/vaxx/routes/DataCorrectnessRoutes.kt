package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dto.request.DataCorrectnessDtoIn
import blue.mild.covid.vaxx.dto.request.query.DataCorrectnessConfirmationIdDtoIn
import blue.mild.covid.vaxx.dto.request.query.PatientIdQueryDtoIn
import blue.mild.covid.vaxx.dto.response.DataCorrectnessConfirmationDetailDtoOut
import blue.mild.covid.vaxx.extensions.asContextAware
import blue.mild.covid.vaxx.extensions.closestDI
import blue.mild.covid.vaxx.extensions.createLogger
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.service.DataCorrectnessService
import blue.mild.covid.vaxx.service.IsinServiceInterface
import blue.mild.covid.vaxx.service.PatientService
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
fun NormalOpenAPIRoute.dataCorrectnessRoutes() {
    val logger = createLogger("DataCorrectnessRoutes")

    val dataCorrectnessService by closestDI().instance<DataCorrectnessService>()
    val patientService by closestDI().instance<PatientService>()
    val isinService by closestDI().instance<IsinServiceInterface>()

    authorizeRoute {
        route(Routes.dataCorrectness) {
            get<DataCorrectnessConfirmationIdDtoIn, DataCorrectnessConfirmationDetailDtoOut, UserPrincipal>(
                info("Get detail about data correctness check for given data ID.")
            ) { (dataCorrectnessId) ->
                val principal = principal()
                logger.info { "Data correctness request from user ${principal.userId} for correctness ID: $dataCorrectnessId." }
                respond(dataCorrectnessService.get(dataCorrectnessId))
            }

            get<PatientIdQueryDtoIn, DataCorrectnessConfirmationDetailDtoOut, UserPrincipal>(
                info("Get detail about data correctness check for given patient ID.")
            ) { (patientId) ->
                val principal = principal()
                logger.info { "Data correctness request from user ${principal.userId} for patient ID: $patientId." }
                respond(dataCorrectnessService.getForPatient(patientId))
            }
        }
    }

    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN, UserRole.DOCTOR, UserRole.RECEPTIONIST)) {
        route(Routes.dataCorrectness) {
            post<Unit, DataCorrectnessConfirmationDetailDtoOut, DataCorrectnessDtoIn, UserPrincipal>(
                info("Register that the data about patient are correct. Requires ADMIN, DOCTOR or RECEPTIONIST role.")
            ) { _, request ->
                val principal = principal()
                logger.info { "User ${principal.userId} verified data for ${request.patientId} with result ${request.dataAreCorrect}." }

                // Try to export patient information in ISIN
                val patient = patientService.getPatientById(request.patientId)
                val wasExportedToIsin = isinService.tryExportPatientContactInfo(patient, notes = request.notes)

                val correctnessId = dataCorrectnessService.registerCorrectness(asContextAware(request), wasExportedToIsin)
                logger.info { "Correctness saved successfully under id $correctnessId." }
                respond(dataCorrectnessService.get(correctnessId))
            }
        }
    }
}
