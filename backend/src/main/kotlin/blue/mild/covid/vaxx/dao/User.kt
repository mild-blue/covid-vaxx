package blue.mild.covid.vaxx.dao

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp
import java.time.Instant

object User : Table("users") {
    val id = varchar("id", 36)
    val created: Column<Instant> = timestamp("created")
    val updated: Column<Instant> = timestamp("updated")

    val username = varchar("username", 128)
    val passwordHash = varchar("password_hash", 128)
    val role = enumerationByName("role", 16, UserRole::class)

    override val primaryKey = PrimaryKey(id)
}
