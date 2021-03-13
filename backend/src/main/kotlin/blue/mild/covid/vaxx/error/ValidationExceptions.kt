package blue.mild.covid.vaxx.error

sealed class ValidationException(
    override val message: String
) : Exception(message)

data class PropertyValidationException(
    val parameterName: String,
    val parameterValue: Any
) : ValidationException("Parameter $parameterName = $parameterValue is not valid.")

data class EmptyStringException(
    val parameterName: String,
) : ValidationException("Parameter $parameterName must not be empty.")
