package blue.mild.covid.vaxx.platform

import io.ktor.client.HttpClient
import io.ktor.client.features.HttpClientFeature
import io.ktor.client.statement.HttpReceivePipeline
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.util.AttributeKey
import mu.KLogging

/**
 * [ClientRequestMetric] callback.
 */
typealias RequestMetricHandler = suspend (RequestMetric) -> Unit


/**
 * Enables callback after HttpClient sends a request with [RequestMetric].
 */
class ClientRequestMetric(private val metricHandler: RequestMetricHandler) {

    class Config {
        internal var metricHandler: RequestMetricHandler = {}

        /**
         * Set [RequestMetricHandler] called at the end of the request.
         */
        fun onResponse(block: RequestMetricHandler) {
            metricHandler = block
        }
    }

    companion object Feature : HttpClientFeature<Config, ClientRequestMetric>, KLogging() {

        override val key: AttributeKey<ClientRequestMetric> =
            AttributeKey("ClientRequestMetric")

        override fun prepare(block: Config.() -> Unit) =
            ClientRequestMetric(Config().apply(block).metricHandler)

        override fun install(feature: ClientRequestMetric, scope: HttpClient) {
            // synchronous response pipeline hook
            // instead of ResponseObserver - which spawns a new coroutine
            scope.receivePipeline.intercept(HttpReceivePipeline.After) { response ->
                // WARNING: Do not consume HttpResponse.content here,
                // or you will corrupt the client response.
                runCatching { feature.metricHandler(response.toRequestMetric()) }
                    .onFailure { logger.error(it) { "Error during metering!" } }
            }
        }

        // does not touch the content
        private fun HttpResponse.toRequestMetric() = RequestMetric(
            requestTime = requestTime.timestamp,
            responseTime = responseTime.timestamp,
            method = request.method.value,
            url = request.url.toString(),
            responseCode = status.value
        )
    }
}


data class RequestMetric(
    // number of epoch milliseconds
    val requestTime: Long,
    val responseTime: Long,
    val method: String,
    val url: String,
    val responseCode: Int
)

fun RequestMetric.requestDurationInMillis() = responseTime - requestTime
fun RequestMetric.requestDurationInSeconds() = requestDurationInMillis() / 1000.0
