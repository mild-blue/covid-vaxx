package blue.mild.covid.vaxx.dao.model

/**
 * Table which contains personal information for personnel working with the system.
 */
open class PersonnelTable(name: String) : ManagedTable(name) {
    /**
     * First name.
     */
    val firstName = varchar("first_name", DatabaseTypeLength.DEFAULT_STRING)

    /**
     * Last name.
     */
    val lastName = varchar("last_name", DatabaseTypeLength.DEFAULT_STRING)

    /**
     * Validated email address.
     */
    val email = varchar("email", DatabaseTypeLength.DEFAULT_STRING).uniqueIndex()
}
