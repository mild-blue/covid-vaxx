package blue.mild.covid.vaxx.dao.model

object Locations : ManagedTable("locations") {
    /**
     * Address
     *
     * We have other attributes zipCode & district to make matching with patient simpler
     */
    val address = varchar("address", DatabaseTypeLength.DEFAULT_STRING)

    /**
     * City zip code in form xxxyy.
     */
    val zipCode = integer("zip_code")

    /**
     * City district.
     */
    val district = varchar("district", DatabaseTypeLength.SHORT_STRING)

    /**
     * Validated phone number in format +420xxxyyyzzz
     */
    val phoneNumber = varchar("phone_number", DatabaseTypeLength.PHONE_NUMBER).nullable()

    /**
     * Validated email address.
     */
    val email = varchar("email", DatabaseTypeLength.DEFAULT_STRING).nullable()

    /**
     * Indication about patient - ie. chronic disease or teacher.
     */
    val notes = text("notes").nullable()


}
