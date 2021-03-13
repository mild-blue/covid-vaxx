package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp

object Question : Table("questions") {
    val id = varchar("id", DatabaseTypeLength.ID)
    val created = timestamp("created")
    val updated = timestamp("updated")

    val placeholder = varchar("placeholder", DatabaseTypeLength.DEFAULT_STRING)
    val cs = text("cs")
    val eng = text("eng")

    override val primaryKey = PrimaryKey(id)
}
