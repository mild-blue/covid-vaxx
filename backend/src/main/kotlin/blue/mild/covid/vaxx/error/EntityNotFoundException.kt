package blue.mild.covid.vaxx.error

import java.util.UUID

inline fun <reified T> entityNotFound(id: UUID) = EntityNotFoundException(T::class.simpleName ?: "", id)

data class EntityNotFoundException(val entityName: String, val entityId: UUID) :
    Exception("Entity $entityName with id $entityId does not exist.")
