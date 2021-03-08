package blue.mild.covid.vaxx.dto.config

data class CorsConfigurationDto(
    val enableCors: Boolean,
    val allowedHosts: List<Host>
) {
    data class Host(val scheme: String, val domain: String)
}
