package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.UserRole
import blue.mild.covid.vaxx.dto.request.CaptchaVerificationDtoIn
import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import blue.mild.covid.vaxx.dto.request.UserRegistrationDtoIn
import blue.mild.covid.vaxx.dto.response.BearerTokenDtoOut
import blue.mild.covid.vaxx.extensions.di
import blue.mild.covid.vaxx.extensions.respond
import blue.mild.covid.vaxx.security.auth.JwtService
import blue.mild.covid.vaxx.security.auth.PatientPrincipal
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.service.UserService
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.post
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
    val userService by di().instance<UserService>()
    val jwtService by di().instance<JwtService>()

    route(Routes.registeredUserLogin) {
        post<Unit, BearerTokenDtoOut, LoginDtoIn>(
            info("Login endpoint for the registered users such as administrators and doctors.")
        ) { _, loginDto ->
            val principal = userService.verifyCredentials(loginDto)
            respond(jwtService.generateToken(principal))
        }
    }

    route(Routes.registrationCaptcha) {
        post<Unit, BearerTokenDtoOut, CaptchaVerificationDtoIn>(
            info("Endpoint that issues access to the API for the non-registered users.")
        ) { _, (_) ->
            // TODO somehow verify token with Google
            respond(jwtService.generateToken(PatientPrincipal))
        }
    }

    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN)) {
        route(Routes.userRegistration) {
            post<Unit, Unit, UserRegistrationDtoIn, UserPrincipal>(
                info("Register new user of the system.")
            ) { _, registration ->
                userService.registerUser(registration)
                respond(HttpStatusCode.OK)
            }
        }
    }
}