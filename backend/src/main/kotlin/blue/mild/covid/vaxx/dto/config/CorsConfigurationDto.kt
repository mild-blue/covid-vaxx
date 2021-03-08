package blue.mild.covid.vaxx.dto.config

data class CorsConfigurationDto(
    val enableCors: Boolean,
    val allowedHosts: List<String>
)
