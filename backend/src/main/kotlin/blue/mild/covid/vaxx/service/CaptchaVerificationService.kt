package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.config.ReCaptchaVerificationConfigurationDto
import blue.mild.covid.vaxx.security.auth.CaptchaFailedException
import blue.mild.covid.vaxx.security.auth.PatientPrincipal
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import mu.KLogging
import java.time.Instant

class CaptchaVerificationService(
    private val client: HttpClient,
    private val configurationDto: ReCaptchaVerificationConfigurationDto
) {

    private companion object : KLogging()

    /**
     * Verify [token] from Google captcha, if the token is valid, returns [PatientPrincipal].
     * Otherwise throws Ca
     */
    suspend fun verify(token: String, host: String? = null) {
        val captchaResponse = runCatching {
            client.post<CaptchaResponseDto>(host = "https://www.google.com/recaptcha/api/siteverify") {
                parameter("secret", configurationDto.secretKey)
                parameter("response", token)
                parameter("remoteip", host)
            }
        }.onFailure {
            logger.error(it) { "Captcha call to Google failed." }
        }.getOrNull() ?: throw CaptchaFailedException()

        if (!captchaResponse.success) {
            logger.warn { "Google returned: $captchaResponse" }
            throw CaptchaFailedException()
        }
    }
}


data class CaptchaResponseDto(
    val success: Boolean,
    val challengeTs: Instant,
    val hostName: String,
    val errorCodes: List<String> = emptyList()
)
