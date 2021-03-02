package blue.mild.covid.vaxx.dao

import org.jetbrains.exposed.sql.Table

object Question : Table() {
    val id = varchar("id", 36)

    val placeholder = varchar("placeholder", 256)

    val cs = text("cs")
    val eng = text("eng")

    override val primaryKey = PrimaryKey(id)
}
