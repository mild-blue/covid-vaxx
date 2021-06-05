package blue.mild.covid.vaxx.error

import blue.mild.covid.vaxx.dto.internal.IsinValidationResultDto

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
    val validationResult: IsinValidationResultDto
    ) : ValidationException("Problem occurred during isin validation: ${validationResult.status}")

class EmptyUpdateException : ValidationException("No data given for the update.")

class NoPersonalAndInsuranceNumberException :
    ValidationException("Personal number or insurance number have to be specified.")
