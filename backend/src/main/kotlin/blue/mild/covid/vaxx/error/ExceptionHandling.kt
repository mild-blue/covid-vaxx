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
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.principal
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.request.path
import io.ktor.server.response.respond
import org.jetbrains.exposed.exceptions.ExposedSQLException

private val logger = createLogger("ExceptionHandler")


/**
 * Registers exception handling.
 */
@Suppress("LongMethod") // it's fine as this is part of KTor
fun Application.installExceptionHandling() {
    install(StatusPages) {
        exception<InsufficientRightsException> { call, ex ->
            logger.warn(ex) {
                call.principal<UserPrincipal>()
                    ?.let { user -> "${user.userId} tried to access resource \"${call.request.path()}\" that is not allowed." }
                    ?: "User without principals tried to access the resource ${call.request.path()}."
            }
            call.respond(HttpStatusCode.Forbidden)
        }

        exception<CaptchaFailedException> { call, ex ->
            logger.warn(ex) { "Captcha verification failed - $ex." }
            call.errorResponse(HttpStatusCode.UnprocessableEntity, "Captcha verification failed.")
        }

        exception<AuthorizationException> { call, ex ->
            logger.warn(ex) { "Authorization failed - $ex." }
            call.respond(HttpStatusCode.Unauthorized)
        }

        exception<EntityNotFoundException> { call, ex ->
            logger.warn(ex) { "Entity was not found - $ex." }
            call.errorResponse(HttpStatusCode.NotFound, ex.message)
        }

        exception<InvalidSlotCreationRequest> { call, ex ->
            logger.error(ex) { "Invalid slot creation request - $ex." }
            call.errorResponse(HttpStatusCode.BadRequest, ex.message)
        }

        exception<NoVaccinationSlotsFoundException> { call, ex ->
            logger.error(ex) { "No vaccination slots found - $ex." }
            call.errorResponse(HttpStatusCode.NotFound, ex.message)
        }

        exception<HttpParametersException> { call, ex ->
            logger.error(ex) { "Invalid HTTP parameters - $ex." }
            call.errorResponse(HttpStatusCode.BadRequest, ex.message)
        }

        jsonExceptions()

        exception<IsinValidationException> { call, ex ->
            logger.warn(ex) { "ISIN validation failed - $ex." }
            call.errorResponse(HttpStatusCode.NotAcceptable, "Not acceptable: ${ex.message}.")
        }

        // validation failed for some property
        exception<ValidationException> { call, ex ->
            logger.warn(ex) { "Validation exception - $ex." }
            call.errorResponse(HttpStatusCode.BadRequest, "Bad request: ${ex.message}.")
        }

        // open api serializer - missing parameters such as headers or query
        exception<OpenAPIRequiredFieldException> { call, ex ->
            logger.error(ex) { "Missing data in request: ${ex.message}." }
            call.errorResponse(HttpStatusCode.BadRequest, "Missing data in request: ${ex.message}.")
        }

        // exception from exposed, during saving to the database
        exception<ExposedSQLException> { call, ex ->
            if (ex.message?.contains("already exists", ignoreCase = true) == true) {
                logger.warn(ex) { "Requested entity already exists - ${ex.message}." }
                call.errorResponse(HttpStatusCode.Conflict, "Entity already exists!.")
            } else {
                logger.error(ex) { "Unknown exposed SQL Exception." }
                call.errorResponse(HttpStatusCode.BadRequest, "Bad request.")
            }
        }

        // generic error handling
        exception<Exception> { call, ex ->
            logger.error(ex) { "Unknown exception occurred in the application: ${ex.message}." }
            call.errorResponse(
                HttpStatusCode.InternalServerError,
                "Server was unable to fulfill the request, please contact administrator with request ID: ${call.callId}."
            )
        }
    }
}

private fun StatusPagesConfig.jsonExceptions() {
    val respond: suspend ApplicationCall.(String) -> Unit = { message ->
        this.errorResponse(HttpStatusCode.BadRequest, message)
    }

    // wrong format of some property
    exception<InvalidFormatException> { call, ex ->
        logger.error(ex) { "Invalid data format." }
        call.respond("Wrong data format.")
    }

    // server received JSON with additional properties it does not know
    exception<UnrecognizedPropertyException> { call, ex ->
        logger.error(ex) { "Unrecognized property in the JSON." }
        call.respond("Unrecognized body property ${ex.propertyName}.")
    }

    // missing data in the request
    exception<MissingKotlinParameterException> { call, ex ->
        logger.error(ex) { "Missing parameter in the request: ${ex.message}." }
        call.respond("Missing parameter: ${ex.parameter}.")
    }

    // generic, catch-all exception from jackson serialization
    exception<JacksonException> { call, ex ->
        logger.error(ex) { "Could not deserialize data: ${ex.message}." }
        call.respond("Bad request, could not deserialize data.")
    }
}

private suspend inline fun ApplicationCall.errorResponse(statusCode: HttpStatusCode, message: String?) {
    respond(status = statusCode, ErrorResponseDto(message ?: "No details specified.", callId))
}
