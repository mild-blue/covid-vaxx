package blue.mild.covid.vaxx.service

import java.util.UUID

class EntityIdProvider {
    /**
     * Generate id for the database entity.
     */
    fun generateId(): UUID = UUID.randomUUID()
}
