package blue.mild.covid.vaxx.error

import blue.mild.covid.vaxx.security.auth.AuthorizationException
import blue.mild.covid.vaxx.security.auth.CaptchaFailedException
import blue.mild.covid.vaxx.security.auth.InsufficientRightsException
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.utils.createLogger
import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.papsign.ktor.openapigen.exceptions.OpenAPIRequiredFieldException
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
import io.ktor.util.pipeline.PipelineContext
import org.jetbrains.exposed.exceptions.ExposedSQLException

private val logger = createLogger("ExceptionHandler")


/**
 * Registers exception handling.
 */
fun Application.installExceptionHandling() {
    install(StatusPages) {
        // TODO define correct logging level policy

        exception<InsufficientRightsException> {
            logger.warn {
                call.principal<UserPrincipal>()
                    ?.let { "$it tried to access resource \"${call.request.path()}\" that is not allowed." }
                    ?: "User without principals tried to access the resource ${call.request.path()}."
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
            logger.debug { cause.message }
            call.errorResponse(HttpStatusCode.NotFound, cause.message)
        }

        deserializationExceptions()

        // validation failed for some property
        exception<ValidationException> { cause ->
            logger.debug { cause.message }
            call.errorResponse(HttpStatusCode.BadRequest, "Bad request: ${cause.message}.")
        }

        // open api serializer - missing parameters such as headers or query
        exception<OpenAPIRequiredFieldException> { cause ->
            logger.debug { "Missing data in request: ${cause.message}" }
            call.errorResponse(HttpStatusCode.BadRequest, "Missing data in request: ${cause.message}.")
        }

        // exception from exposed, during saving to the database
        exception<ExposedSQLException> { cause ->
            logger.warn { "Attempt to store invalid data to the database: ${cause.message}." }
            if (cause.message?.contains("already exists") == true) {
                call.errorResponse(HttpStatusCode.Conflict, "Entity already exists!.")
            } else {
                call.errorResponse(HttpStatusCode.BadRequest, "Bad request.")
            }
        }

        // generic error handling
        exception<Exception> { cause ->
            logger.error(cause) { "Exception occurred in the application: ${cause.message}." }
            call.errorResponse(
                HttpStatusCode.InternalServerError,
                "Server was unable to fulfill the request, please contact administrator with request ID: ${call.callId}."
            )
        }
    }
}

private fun StatusPages.Configuration.deserializationExceptions() {
    val respond: suspend PipelineContext<Unit, ApplicationCall>.(String) -> Unit = { message ->
        call.errorResponse(HttpStatusCode.BadRequest, message)
    }

    // wrong format of some property
    exception<InvalidFormatException> { cause ->
        logger.debug { cause.message }
        respond("Wrong data format.")
    }

    // server received JSON with additional properties it does not know
    exception<UnrecognizedPropertyException> { cause ->
        respond("Unrecognized body property ${cause.propertyName}.")
    }

    // missing data in the request
    exception<MissingKotlinParameterException> { cause ->
        logger.debug { "Missing parameter in the request: ${cause.message}" }
        respond("Missing parameter: ${cause.parameter}.")
    }

    // generic, catch-all exception from jackson serialization
    exception<JacksonException> { cause ->
        logger.debug { cause.message }
        respond("Bad request, could not deserialize data.")
    }
}

private suspend inline fun ApplicationCall.errorResponse(statusCode: HttpStatusCode, message: String?) {
    respond(status = statusCode, ErrorResponseDto(message ?: "No details specified.", callId))
}
