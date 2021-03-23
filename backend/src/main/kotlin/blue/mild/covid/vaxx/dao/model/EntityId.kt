package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import java.util.UUID

/**
 * Represents format for IDs.
 */
typealias EntityId = UUID

/**
 * Creates ID column.
 */
fun Table.entityId(name: String): Column<EntityId> = uuid(name)
