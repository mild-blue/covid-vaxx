package blue.mild.covid.vaxx.dto.request

import com.papsign.ktor.openapigen.annotations.parameters.HeaderParam

// TODO correct header name
data class CaptchaVerificationDtoIn(
    @HeaderParam("Token from the Google Captcha.")
    val recaptchaToken: String
)

