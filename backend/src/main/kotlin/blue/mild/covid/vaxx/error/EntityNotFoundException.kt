package blue.mild.covid.vaxx.error

import kotlin.reflect.KProperty

inline fun <reified T> entityNotFound(parameter: KProperty<*>, value: Any) =
    EntityNotFoundException(T::class.simpleName ?: "", parameter.name, value)

data class EntityNotFoundException(val entityName: String, val parameterName: String, val parameterValue: Any) :
    Exception("Entity $entityName with $parameterName = $parameterValue does not exist.")
