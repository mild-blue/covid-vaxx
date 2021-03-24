package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.Table

/**
 * Table that has as a primary key [EntityId].
 */
open class IdTable(name: String) : Table(name) {
    /**
     * Primary key.
     */
    val id = entityId("id").autoGenerate()

    override val primaryKey = PrimaryKey(id)
}
