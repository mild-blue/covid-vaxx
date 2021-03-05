package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.auth.UserPrincipal
import blue.mild.covid.vaxx.auth.authorizeRoute
import blue.mild.covid.vaxx.dao.UserRole
import blue.mild.covid.vaxx.dto.request.PatientCreatedDtoOut
import blue.mild.covid.vaxx.dto.request.PatientIdDtoIn
import blue.mild.covid.vaxx.dto.request.PatientQueryDtoIn
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.dto.response.PatientDeletedDtoOut
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import blue.mild.covid.vaxx.extensions.di
import blue.mild.covid.vaxx.service.PatientService
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.delete
import com.papsign.ktor.openapigen.route.path.auth.get
import com.papsign.ktor.openapigen.route.path.auth.post
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.delete
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.kodein.di.instance
import pw.forst.tools.katlib.asList

/**
 * Routes related to patient entity.
 */
fun NormalOpenAPIRoute.patientRoutes() {
    // TODO #70 delete this, use just authorized routes
    openRoutes()
    authorizedRoutes()
}

// TODO #70 delete this
private fun NormalOpenAPIRoute.openRoutes() {
    val patientService by di().instance<PatientService>()
    route(Routes.patient) {
        get<PatientIdDtoIn, PatientDtoOut>(
            info("Get user by ID.")
        ) { (id) ->
            respond(patientService.getPatientById(id))
        }

        delete<PatientIdDtoIn, PatientDeletedDtoOut>(
            info("Delete user by ID.")
        ) { (id) ->
            respond(patientService.deletePatientById(id))
        }

        get<PatientQueryDtoIn, List<PatientDtoOut>>(
            info("Search endpoint for user, only single parameter is taken in account.")
        ) { patientQuery ->
            val response = when {
                patientQuery.id != null -> patientService.getPatientById(patientQuery.id).asList()
                patientQuery.personalNumber != null -> patientService.getPatientsByPersonalNumber(patientQuery.personalNumber)
                patientQuery.email != null -> patientService.getPatientsByEmail(patientQuery.email)
                else -> patientService.getAllPatients()
            }
            respond(response)
        }

        post<Unit, PatientCreatedDtoOut, PatientRegistrationDtoIn>(
            info("Save patient registration to the database.")
        ) { _, patientRegistration ->
            respond(patientService.savePatient(patientRegistration))
        }

    }

}

// TODO #70 delete authorized suffix
private fun NormalOpenAPIRoute.authorizedRoutes() {
    val patientService by di().instance<PatientService>()
    // routes for registered users only
    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN, UserRole.DOCTOR)) {
        route("${Routes.patient}/authorized") {
            get<PatientIdDtoIn, PatientDtoOut, UserPrincipal>(
                info("Get user by ID.")
            ) { (id) ->
                respond(patientService.getPatientById(id))
            }

            delete<PatientIdDtoIn, PatientDeletedDtoOut, UserPrincipal>(
                info("Delete user by ID.")
            ) { (id) ->
                respond(patientService.deletePatientById(id))
            }

            get<PatientQueryDtoIn, List<PatientDtoOut>, UserPrincipal>(
                info("Search endpoint for user, only single parameter is taken in account.")
            ) { patientQuery ->
                val response = when {
                    patientQuery.id != null -> patientService.getPatientById(patientQuery.id).asList()
                    patientQuery.personalNumber != null -> patientService.getPatientsByPersonalNumber(patientQuery.personalNumber)
                    patientQuery.email != null -> patientService.getPatientsByEmail(patientQuery.email)
                    else -> patientService.getAllPatients()
                }
                respond(response)
            }
        }
    }
    // routes that are authorized for users that passed captchas
    authorizeRoute {
        route("${Routes.patient}/authorized") {
            post<Unit, PatientCreatedDtoOut, PatientRegistrationDtoIn, UserPrincipal>(
                info("Save patient registration to the database.")
            ) { _, patientRegistration ->
                respond(patientService.savePatient(patientRegistration))
            }
        }
    }

}
