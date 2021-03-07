package blue.mild.covid.vaxx.security.ratelimiting

data class RateLimitingException(
    val retryAfterSeconds: Long,
    val remoteHost: String,
    override val message: String = "Rate limit hit for host $remoteHost - retry after ${retryAfterSeconds}s."
) : Exception(message)
