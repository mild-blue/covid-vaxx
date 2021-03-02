package blue.mild.covid.vaxx.dao

import org.jetbrains.exposed.sql.Table

object Patient : Table() {
    val id = varchar("id", 36)
    val name = varchar("name", length = 256)

    override val primaryKey = PrimaryKey(id)
}
