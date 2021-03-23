package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp

object User : Table("users") {
    /**
     * Primary key.
     */
    val id = entityId("id")

    /**
     * When this record was created.
     */
    val created = timestamp("created")

    /**
     * When this record was updated. By default same value as [created].
     */
    val updated = timestamp("updated")

    /**
     * Usually an email address.
     */
    val username = varchar("username", DatabaseTypeLength.SHORT_STRING)

    /**
     * SCrypt hash of the password.
     */
    val passwordHash = varchar("password_hash", DatabaseTypeLength.SHORT_STRING)

    /**
     * User role.
     */
    val role = enumerationByName("role", DatabaseTypeLength.ROLE, UserRole::class)

    override val primaryKey = PrimaryKey(id)
}
