package blue.mild.covid.vaxx.auth

data class JwtConfigurationDto(
    val realm: String,
    val issuer: String,
    val audience: String,
    val expirationInMinutes: Int,
    val signingSecret: String
)
