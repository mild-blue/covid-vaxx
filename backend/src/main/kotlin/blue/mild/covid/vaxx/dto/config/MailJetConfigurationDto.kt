package blue.mild.covid.vaxx.dto.config

/**
 * Simple configuration for the mail jet API.
 */
data class MailJetConfigurationDto(
    /**
     * Key for API access.
     */
    val apiKey: String,
    /**
     * Secret for API access.
     */
    val apiSecret: String,
    /**
     * Email address to send the emails from Must be verified!
     */
    val emailFrom: String,
    /**
     * Name to give to the email address to send the emails from.
     */
    val nameFrom: String
)
