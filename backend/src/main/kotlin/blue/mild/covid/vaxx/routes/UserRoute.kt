package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.UserRole
import blue.mild.covid.vaxx.dto.request.CaptchaVerificationDtoIn
import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import blue.mild.covid.vaxx.dto.request.UserRegistrationDtoIn
import blue.mild.covid.vaxx.dto.response.BearerTokenDtoOut
import blue.mild.covid.vaxx.dto.response.UserRegisteredDtoOut
import blue.mild.covid.vaxx.extensions.di
import blue.mild.covid.vaxx.security.auth.CaptchaAuthenticationService
import blue.mild.covid.vaxx.security.auth.JwtService
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.service.UserService
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.post
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.kodein.di.instance

/**
 * Routes associated with the user logins and authorizations.
 */
fun NormalOpenAPIRoute.userRoutes() {
    val userService by di().instance<UserService>()
    val jwtService by di().instance<JwtService>()
    val captchaAuthenticationService by di().instance<CaptchaAuthenticationService>()

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
        ) { _, (token) ->
            val principal = captchaAuthenticationService.authenticate(token)
            respond(jwtService.generateToken(principal))
        }
    }

    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN)) {
        route(Routes.userRegistration) {
            post<Unit, UserRegisteredDtoOut, UserRegistrationDtoIn, UserPrincipal>(
                info("Register new user of the system.")
            ) { _, registration ->
                respond(userService.registerUser(registration))
            }
        }
    }
}
