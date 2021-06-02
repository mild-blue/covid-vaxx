package blue.mild.covid.vaxx.error

import blue.mild.covid.vaxx.dto.request.CreateVaccinationSlotsDtoIn
import kotlin.reflect.KProperty

inline fun <reified T> entityNotFound(parameter: KProperty<*>, value: Any) =
    EntityNotFoundException(T::class.simpleName ?: "", parameter.name, value)

data class EntityNotFoundException(val entityName: String, val parameterName: String, val parameterValue: Any) :
    Exception("Entity $entityName with $parameterName = $parameterValue does not exist.")

data class InvalidSlotCreationRequest(override val message: String, val entity: CreateVaccinationSlotsDtoIn) : Exception(message)

data class NoVaccinationSlotsFoundException(override val message: String = "No slots available for given query.") : Exception(message)
