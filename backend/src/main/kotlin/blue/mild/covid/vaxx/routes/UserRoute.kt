package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.auth.JwtService
import blue.mild.covid.vaxx.auth.PatientPrincipal
import blue.mild.covid.vaxx.service.UserLoginService
import com.papsign.ktor.openapigen.annotations.parameters.HeaderParam
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.kodein.di.LazyDI
import org.kodein.di.instance

data class LoginDtoIn(val username: String, val password: String)

data class JwtResponseDtoOut(val token: String)

// TODO correct header name
data class CaptchaVerificationDtoIn(@HeaderParam("") val token: String)

fun NormalOpenAPIRoute.userRoutes(di: LazyDI) {
    val userLoginService by di.instance<UserLoginService>()
    val jwtService by di.instance<JwtService>()

    route(Routes.registeredUserLogin).post<Unit, JwtResponseDtoOut, LoginDtoIn> { _, loginDto ->
        val principal = userLoginService.verifyCredentials(loginDto.username, loginDto.password)
        respond(JwtResponseDtoOut(jwtService.generateToken(principal)))
    }

    route(Routes.registrationCaptcha).post<Unit, JwtResponseDtoOut, CaptchaVerificationDtoIn> { _, (_) ->
        // TODO somehow verify token with Google
        respond(JwtResponseDtoOut(jwtService.generateToken(PatientPrincipal)))
    }
}
