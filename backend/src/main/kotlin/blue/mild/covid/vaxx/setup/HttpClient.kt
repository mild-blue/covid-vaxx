package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.utils.createLogger
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging

/**
 * Prepares HTTP Client.
 */
fun createHttpClient() =
    HttpClient(Apache) {
        install(JsonFeature) {
            serializer = JacksonSerializer {
                registerModule(JavaTimeModule())
            }
        }

        install(Logging) {
            logger = Logger.TRACE
            level = LogLevel.ALL
        }
    }


/**
 * Trace logger for HTTP Requests.
 */
private val Logger.Companion.TRACE: Logger
    get() = object : Logger, org.slf4j.Logger by createLogger("HttpClient") {
        override fun log(message: String) {
            trace(message)
        }
    }
