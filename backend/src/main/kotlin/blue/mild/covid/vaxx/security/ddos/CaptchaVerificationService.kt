package blue.mild.covid.vaxx.security.ddos

import blue.mild.covid.vaxx.dto.config.ReCaptchaVerificationConfigurationDto
import blue.mild.covid.vaxx.security.auth.CaptchaFailedException
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import mu.KLogging
import java.time.Instant

class CaptchaVerificationService(
    private val client: HttpClient,
    private val configurationDto: ReCaptchaVerificationConfigurationDto
) : RequestVerificationService {

    private companion object : KLogging()

    /**
     * Verify [token] from Google captcha, if the token is valid, returns [UserPrincipal].
     * Otherwise throws Ca
     */
    override suspend fun verify(token: String, host: String?) {
        val captchaResponse = runCatching {
            client.get(configurationDto.googleUrl) {
                parameter("secret", configurationDto.secretKey)
                parameter("response", token)
                parameter("remoteip", host)
            }.body<CaptchaResponseDto>()
        }.onFailure {
            logger.error(it) { "Captcha call to Google failed." }
        }.getOrNull() ?: throw CaptchaFailedException()

        if (!captchaResponse.success) {
            logger.warn { "Google returned: $captchaResponse" }
            throw CaptchaFailedException()
        }
        // TODO maybe add more verifications with regards to timestamp and hostname
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class CaptchaResponseDto(
        val success: Boolean,
        @JsonProperty("challenge_ts")
        val challengeTimestamp: Instant?,
        val hostname: String?,
        val score: Double?,
        @JsonProperty("error-codes")
        val errorCodes: List<String>?
    )
}
