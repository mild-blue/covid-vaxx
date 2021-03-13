package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp

object User : Table("users") {
    val id = varchar("id", DatabaseTypeLength.ID)
    val created = timestamp("created")
    val updated = timestamp("updated")

    val username = varchar("username", DatabaseTypeLength.SHORT_STRING)
    val passwordHash = varchar("password_hash", DatabaseTypeLength.SHORT_STRING)
    val role = enumerationByName("role", DatabaseTypeLength.ROLE, UserRole::class)

    override val primaryKey = PrimaryKey(id)
}
