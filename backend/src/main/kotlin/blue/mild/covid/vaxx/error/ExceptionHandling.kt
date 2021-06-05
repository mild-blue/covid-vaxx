package blue.mild.covid.vaxx.error

import blue.mild.covid.vaxx.extensions.createLogger
import blue.mild.covid.vaxx.security.auth.AuthorizationException
import blue.mild.covid.vaxx.security.auth.CaptchaFailedException
import blue.mild.covid.vaxx.security.auth.InsufficientRightsException
import blue.mild.covid.vaxx.security.auth.UserPrincipal
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
@Suppress("LongMethod") // it's fine as this is part of KTor
fun Application.installExceptionHandling() {
    install(StatusPages) {
        exception<InsufficientRightsException> {
            logger.warn(it) {
                call.principal<UserPrincipal>()
                    ?.let { user -> "${user.userId} tried to access resource \"${call.request.path()}\" that is not allowed." }
                    ?: "User without principals tried to access the resource ${call.request.path()}."
            }
            call.respond(HttpStatusCode.Forbidden)
        }

        exception<CaptchaFailedException> {
            logger.warn(it) { "Captcha verification failed - $it.¬" }
            call.errorResponse(HttpStatusCode.UnprocessableEntity, "Captcha verification failed.")
        }

        exception<AuthorizationException> {
            logger.warn(it) { "Authorization failed - $it." }
            call.respond(HttpStatusCode.Unauthorized)
        }

        exception<EntityNotFoundException> {
            logger.warn(it) { "Entity was not found - $it." }
            call.errorResponse(HttpStatusCode.NotFound, it.message)
        }

        exception<InvalidSlotCreationRequest> {
            logger.error(it) { "Invalid slot creation request - $it." }
            call.errorResponse(HttpStatusCode.BadRequest, it.message)
        }

        exception<NoVaccinationSlotsFoundException> {
            logger.error(it) { "No vaccination slots found - $it." }
            call.errorResponse(HttpStatusCode.NotFound, it.message)
        }

        jsonExceptions()

        exception<IsinValidationException> {
            logger.warn(it) { "ISIN validation failed - $it." }
            call.errorResponse(HttpStatusCode.NotAcceptable, "Not acceptable: ${it.message}.")
        }

        // validation failed for some property
        exception<ValidationException> {
            logger.warn(it) { "Validation exception - $it." }
            call.errorResponse(HttpStatusCode.BadRequest, "Bad request: ${it.message}.")
        }

        // open api serializer - missing parameters such as headers or query
        exception<OpenAPIRequiredFieldException> {
            logger.error { "Missing data in request: ${it.message}." }
            call.errorResponse(HttpStatusCode.BadRequest, "Missing data in request: ${it.message}.")
        }

        // exception from exposed, during saving to the database
        exception<ExposedSQLException> {
            if (it.message?.contains("already exists", ignoreCase = true) == true) {
                logger.warn(it) { "Requested entity already exists - ${it.message}." }
                call.errorResponse(HttpStatusCode.Conflict, "Entity already exists!.")
            } else {
                logger.error(it) { "Unknown exposed SQL Exception." }
                call.errorResponse(HttpStatusCode.BadRequest, "Bad request.")
            }
        }

        // generic error handling
        exception<Exception> {
            logger.error(it) { "Unknown exception occurred in the application: ${it.message}." }
            call.errorResponse(
                HttpStatusCode.InternalServerError,
                "Server was unable to fulfill the request, please contact administrator with request ID: ${call.callId}."
            )
        }
    }
}

private fun StatusPages.Configuration.jsonExceptions() {
    val respond: suspend PipelineContext<Unit, ApplicationCall>.(String) -> Unit = { message ->
        call.errorResponse(HttpStatusCode.BadRequest, message)
    }

    // wrong format of some property
    exception<InvalidFormatException> {
        logger.error(it) { "Invalid data format." }
        respond("Wrong data format.")
    }

    // server received JSON with additional properties it does not know
    exception<UnrecognizedPropertyException> {
        logger.error(it) { "Unrecognized property in the JSON." }
        respond("Unrecognized body property ${it.propertyName}.")
    }

    // missing data in the request
    exception<MissingKotlinParameterException> {
        logger.error(it) { "Missing parameter in the request: ${it.message}." }
        respond("Missing parameter: ${it.parameter}.")
    }

    // generic, catch-all exception from jackson serialization
    exception<JacksonException> {
        logger.error(it) { "Could not deserialize data: ${it.message}." }
        respond("Bad request, could not deserialize data.")
    }
}

private suspend inline fun ApplicationCall.errorResponse(statusCode: HttpStatusCode, message: String?) {
    respond(status = statusCode, ErrorResponseDto(message ?: "No details specified.", callId))
}
