package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.EntityId
import java.util.UUID

class EntityIdProvider {
    /**
     * Generate id for the database entity.
     */
    fun generateId(): EntityId = UUID.randomUUID()
}
