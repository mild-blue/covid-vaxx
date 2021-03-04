package blue.mild.covid.vaxx.dao

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp
import java.time.Instant

object Question : Table("questions") {
    val id = varchar("id", 36)
    val created: Column<Instant> = timestamp("created")
    val updated: Column<Instant?> = timestamp("updated").nullable()
    val deleted: Column<Instant?> = timestamp("deleted").nullable()

    val placeholder = varchar("placeholder", 256)

    val cs = text("cs")
    val eng = text("eng")

    override val primaryKey = PrimaryKey(id)
}
