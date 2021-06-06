package blue.mild.covid.vaxx.error

import blue.mild.covid.vaxx.dto.internal.PatientValidationResultDto

sealed class ValidationException(
    override val message: String
) : Exception(message)

data class PropertyValidationException(
    val parameterName: String,
    val parameterValue: Any?
) : ValidationException("Parameter $parameterName = $parameterValue is not valid.")

data class EmptyStringException(
    val parameterName: String,
) : ValidationException("Parameter $parameterName must not be empty.")

data class IsinValidationException(
    val validationResult: PatientValidationResultDto
) : ValidationException("Problem occurred during ISIN validation: ${validationResult.status}.")

data class EmptyUpdateException(override val message: String = "No data given for the update.") : ValidationException(message)

class NoPersonalAndInsuranceNumberException :
    ValidationException("Personal number or insurance number has to be specified.")
