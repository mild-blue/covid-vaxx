package blue.mild.covid.vaxx.dto.request

import com.papsign.ktor.openapigen.annotations.mapping.OpenAPIName
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam

data class CaptchaVerificationDtoIn(
    @QueryParam("Token from the Google Captcha.")
    @OpenAPIName(NAME)
    val capthaToken: String
) {
    companion object {
        const val NAME = "captcha"
    }
}
