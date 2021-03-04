package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.auth.JwtService
import blue.mild.covid.vaxx.auth.PatientPrincipal
import blue.mild.covid.vaxx.dto.request.CaptchaVerificationDtoIn
import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import blue.mild.covid.vaxx.dto.response.JwtResponseDtoOut
import blue.mild.covid.vaxx.extensions.di
import blue.mild.covid.vaxx.service.UserLoginService
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.kodein.di.LazyDI
import org.kodein.di.instance

/**
 * Routes associated with the user logins and authorizations.
 */
fun NormalOpenAPIRoute.userRoutes() {
    val userLoginService by di().instance<UserLoginService>()
    val jwtService by di().instance<JwtService>()

    route(Routes.registeredUserLogin) {
        post<Unit, JwtResponseDtoOut, LoginDtoIn>(
            info("Login endpoint for the registered users such as administrators and doctors.")
        ) { _, loginDto ->
            val principal = userLoginService.verifyCredentials(loginDto.username, loginDto.password)
            respond(jwtService.generateToken(principal))
        }
    }
    route(Routes.registrationCaptcha) {
        post<Unit, JwtResponseDtoOut, CaptchaVerificationDtoIn>(
            info("Endpoint that issues access to the API for the non-registered users.")
        ) { _, (_) ->
            // TODO somehow verify token with Google
            respond(jwtService.generateToken(PatientPrincipal))
        }
    }
}
