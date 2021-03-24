package blue.mild.covid.vaxx.dao.model

/**
 * User with login rights to this system.
 */
object Users : PersonnelTable("users") {
    /**
     * SCrypt hash of the password.
     */
    val passwordHash = varchar("password_hash", DatabaseTypeLength.SHORT_STRING)

    /**
     * User role.
     */
    val role = enumerationByName("role", DatabaseTypeLength.ROLE, UserRole::class)
}
