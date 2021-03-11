package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp

object Question : Table("questions") {
    val id = varchar("id", 36)
    val created = timestamp("created")
    val updated = timestamp("updated")

    val placeholder = varchar("placeholder", 256)
    val cs = text("cs")
    val eng = text("eng")

    override val primaryKey = PrimaryKey(id)
}
