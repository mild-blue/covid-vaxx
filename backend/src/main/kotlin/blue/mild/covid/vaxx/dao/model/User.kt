package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp

// lengths in the database
@Suppress("MagicNumber")
object User : Table("users") {
    val id = varchar("id", 36)
    val created = timestamp("created")
    val updated = timestamp("updated")

    val username = varchar("username", 128)
    val passwordHash = varchar("password_hash", 128)
    val role = enumerationByName("role", 16, UserRole::class)

    override val primaryKey = PrimaryKey(id)
}
