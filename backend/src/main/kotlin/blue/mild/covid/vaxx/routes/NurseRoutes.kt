package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dao.repository.NurseRepository
import blue.mild.covid.vaxx.dto.request.CredentialsDtoIn
import blue.mild.covid.vaxx.dto.request.NurseCreationDtoIn
import blue.mild.covid.vaxx.dto.response.OK
import blue.mild.covid.vaxx.dto.response.Ok
import blue.mild.covid.vaxx.dto.response.PersonnelDtoOut
import blue.mild.covid.vaxx.extensions.di
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.service.UserService
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.put
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.kodein.di.instance

/**
 * Registers routes related to the nurse entity.
 */
fun NormalOpenAPIRoute.nurseRoutes() {
    val userService by di().instance<UserService>()
    val nurseRepository by di().instance<NurseRepository>()

    route(Routes.nurse) {
        post<Unit, List<PersonnelDtoOut>, CredentialsDtoIn>(
            info("If the given user exist, returns all current nurses in the database.")
        ) { _, request ->
            // verify credentials
            userService.verifyCredentials(email = request.email, password = request.password)
            respond(nurseRepository.getAll())
        }
    }

    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN)) {
        route(Routes.nurse) {
            // RESTFUL: This should be PUT
            put<Unit, Ok, NurseCreationDtoIn, UserPrincipal>(
                info("Creates nurse entity.")
            ) { _, request ->
                // this is ugly, but it is just an admin endpoint
                nurseRepository.saveNurse(
                    firstName = request.firstName.trim(),
                    lastName = request.lastName.trim(),
                    email = request.email.trim()
                )

                respond(OK)
            }
        }
    }
}
