package blue.mild.covid.vaxx.service

import java.util.UUID

class EntityIdProvider {
    fun generateId(): Pair<UUID, String> = UUID.randomUUID().let { it to it.toString() }
}
