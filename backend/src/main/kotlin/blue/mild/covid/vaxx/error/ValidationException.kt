package blue.mild.covid.vaxx.error

data class ValidationException(val parameterName: String, val parameterValue: Any) :
    Exception("Parameter $parameterName = $parameterValue is not valid.")

data class EmptyStringException(val parameterName: String) :
    Exception("Parameter $parameterName must not be empty.")
