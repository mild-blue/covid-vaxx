package blue.mild.covid.vaxx.dto.config

import java.time.Duration

data class RateLimitConfigurationDto(
    val enableRateLimiting: Boolean,
    val rateLimit: Long,
    val rateLimitDuration: Duration
)
