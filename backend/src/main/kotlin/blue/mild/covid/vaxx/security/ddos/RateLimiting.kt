package blue.mild.covid.vaxx.security.ddos

import blue.mild.covid.vaxx.utils.createLogger
import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.ApplicationRequest
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import java.time.Duration

typealias RateLimitExclusion = (request: ApplicationRequest) -> Boolean

private val rateLimitingLogger = createLogger("RateLimiting")

/**
 * Simple rate limiting implementation using [LinearRateLimiter].
 */
class RateLimiting private constructor(
    private val rateLimit: LinearRateLimiter,
    private val keyExtraction: PipelineContext<*, ApplicationCall>.() -> String,
    private val rateLimitExclusion: RateLimitExclusion
) {

    class Configuration {
        var limit: Long = 1000L
        lateinit var resetTime: Duration
        lateinit var keyExtraction: PipelineContext<*, ApplicationCall>.() -> String
        lateinit var requestExclusion: RateLimitExclusion
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, RateLimiting> {

        override val key: AttributeKey<RateLimiting> = AttributeKey("RateLimiting")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): RateLimiting {
            val config = Configuration().apply(configure)
            val limiter = LinearRateLimiter(config.limit, config.resetTime)
            val rateLimiting = RateLimiting(limiter, config.keyExtraction, config.requestExclusion)
            // intercept request at the beginning
            pipeline.intercept(ApplicationCallPipeline.Features) {
                // determine if it is necessary to filter this request or not
                if (rateLimiting.rateLimitExclusion(call.request)) {
                    proceed()
                    return@intercept
                }
                // determine remote host / key in the limiting implementation
                val remoteHost = rateLimiting.keyExtraction(this)
                val retryAfter = rateLimiting.rateLimit.processRequest(remoteHost)
                // if no retryAfter is defined, proceed in the request pipeline
                if (retryAfter == null) {
                    proceed()
                } else {
                    // at this point we want to deny attacker the request
                    // but we also do not want to spend any more resources on processing this request
                    // for that reason we don't throw exception, nor return jsons, but rather finish the request here
                    call.response.header("Retry-After", retryAfter)
                    call.respond(HttpStatusCode.TooManyRequests)
                    rateLimitingLogger.warn { "Rate limit hit for host $remoteHost - retry after ${retryAfter}s." }
                    // finish the request and do not proceed to next step
                    finish()
                }
            }

            return rateLimiting
        }
    }
}
