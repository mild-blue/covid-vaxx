package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.utils.createLogger
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

private val logger = createLogger("ExceptionHandler")

/**
 * Registers exception handling.
 */
fun Application.registerExceptionHandlers() {
    install(StatusPages) {
        exception<Exception> { cause ->
            logger.error(cause) { "Exception occurred in the application: ${cause.message}" }
            call.errorResponse(HttpStatusCode.InternalServerError, cause.message)
        }
    }
}

suspend inline fun ApplicationCall.errorResponse(statusCode: HttpStatusCode, message: String?) {
    respond(status = statusCode, message = mapOf("message" to (message ?: "No details specified")))
}
