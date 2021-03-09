package blue.mild.covid.vaxx.dto.config

data class ReCaptchaVerificationConfigurationDto(
    val secretKey: String,
    val googleUrl: String = "https://www.google.com/recaptcha/api/siteverify"
)
