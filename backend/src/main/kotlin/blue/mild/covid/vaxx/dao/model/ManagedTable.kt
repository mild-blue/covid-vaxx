package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * Table that contains [created] and [updated] fields.
 */
open class ManagedTable(name: String) : IdTable(name) {
    /**
     * When this record was created.
     */
    val created = timestamp("created")

    /**
     * When this record was updated. By default same value as [created].
     */
    val updated = timestamp("updated")
}
