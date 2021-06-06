package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import blue.mild.covid.vaxx.dto.request.UserRegistrationDtoIn
import blue.mild.covid.vaxx.dto.response.UserLoginResponseDtoOut
import blue.mild.covid.vaxx.dto.response.UserRegisteredDtoOut
import blue.mild.covid.vaxx.extensions.asContextAware
import blue.mild.covid.vaxx.extensions.closestDI
import blue.mild.covid.vaxx.extensions.createLogger
import blue.mild.covid.vaxx.extensions.determineRealIp
import blue.mild.covid.vaxx.extensions.request
import blue.mild.covid.vaxx.extensions.respondWithStatus
import blue.mild.covid.vaxx.security.auth.JwtService
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.service.UserService
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.get
import com.papsign.ktor.openapigen.route.path.auth.post
import com.papsign.ktor.openapigen.route.path.auth.principal
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import io.ktor.http.HttpStatusCode
import org.kodein.di.instance

/**
 * Routes associated with the user logins and authorizations.
 */
fun NormalOpenAPIRoute.userRoutes() {
    val userService by closestDI().instance<UserService>()
    val jwtService by closestDI().instance<JwtService>()
    val logger = createLogger("UserRoute")

    route(Routes.registeredUserLogin) {
        post<Unit, UserLoginResponseDtoOut, LoginDtoIn>(
            info("Login endpoint for the registered users such as administrators and doctors.")
        ) { _, loginDto ->
            logger.info { "Login request for ${loginDto.credentials.email} from host ${request.determineRealIp()}." }

            val principal = userService.createPrincipal(asContextAware(loginDto))
            respond(jwtService.generateToken(principal))
        }
    }

    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN)) {
        route(Routes.userRegistration) {
            post<Unit, UserRegisteredDtoOut, UserRegistrationDtoIn, UserPrincipal>(
                info("Register new user of the system.")
            ) { _, registration ->
                val principal = principal()
                logger.info {
                    "User registration for ${registration.email} registered by ${principal.userId} from host ${request.determineRealIp()}."
                }
                respond(userService.registerUser(registration))
            }
        }
    }

    authorizeRoute {
        route(Routes.userLoginVerification) {
            get<Unit, Unit, UserPrincipal>(
                info("Verify that the currently used token is valid. Returns 200 if token is correct, 401 otherwise.")
            ) {
                respondWithStatus(HttpStatusCode.OK)
            }
        }
    }
}
