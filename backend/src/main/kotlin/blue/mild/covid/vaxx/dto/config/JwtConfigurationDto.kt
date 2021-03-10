package blue.mild.covid.vaxx.dto.config

data class JwtConfigurationDto(
    val realm: String,
    val issuer: String,
    val audience: String,
    val jwtExpirationInMinutes: Long,
    val signingSecret: String
)
