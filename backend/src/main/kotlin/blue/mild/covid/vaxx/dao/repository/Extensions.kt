package blue.mild.covid.vaxx.dao.repository

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.statements.UpdateStatement


/**
 * Updates entity statement if [value] is not null.
 */
fun <T> UpdateStatement.updateIfNotNull(value: T?, column: Column<T>) {
    if (value != null) {
        this[column] = value
    }
}
