package blue.mild.covid.vaxx.error

import blue.mild.covid.vaxx.security.auth.AuthorizationException
import blue.mild.covid.vaxx.security.auth.CaptchaFailedException
import blue.mild.covid.vaxx.security.auth.InsufficientRightsException
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.utils.createLogger
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.principal
import io.ktor.features.StatusPages
import io.ktor.features.callId
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.response.respond
import org.postgresql.util.PSQLException

private val logger = createLogger("ExceptionHandler")

/**
 * Registers exception handling.
 */
fun Application.installExceptionHandling() {
    install(StatusPages) {
        exception<InsufficientRightsException> {
            logger.debug {
                call.principal<UserPrincipal>()
                    ?.let { "$it tried to access resource \"${call.request.path()}\" that is not allowed." }
                    ?: "User without principals tried to access the resource ${call.request.path()}"
            }
            call.respond(HttpStatusCode.Forbidden)
        }

        exception<CaptchaFailedException> {
            logger.debug { "Captcha verification failed." }
            call.errorResponse(HttpStatusCode.UnprocessableEntity, "Captcha verification failed.")
        }

        exception<AuthorizationException> {
            call.respond(HttpStatusCode.Unauthorized)
        }

        exception<EntityNotFoundException> { cause ->
            logger.warn { cause.message }
            call.errorResponse(HttpStatusCode.NotFound, "Not found.")
        }

        exception<ValidationException> { cause ->
            logger.warn { cause.message }
            call.errorResponse(HttpStatusCode.BadRequest, "Bad request. ${cause.message}")
        }

        exception<EmptyStringException> { cause ->
            logger.warn { cause.message }
            call.errorResponse(HttpStatusCode.BadRequest, "Bad request. ${cause.message}")
        }

        exception<PSQLException> { cause ->
            logger.error { cause.message }
            call.errorResponse(HttpStatusCode.BadRequest, "Bad request.")
        }

        exception<Exception> { cause ->
            logger.error(cause) { "Exception occurred in the application: ${cause.message}" }
            call.errorResponse(
                HttpStatusCode.InternalServerError,
                "Server was unable to fulfill the request, please contact administrator with request ID: ${call.callId}"
            )
        }
    }
}

suspend inline fun ApplicationCall.errorResponse(statusCode: HttpStatusCode, message: String?) {
    respond(status = statusCode, ErrorResponseDto(message ?: "No details specified", callId))
}
