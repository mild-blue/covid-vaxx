package blue.mild.covid.vaxx.dao.model

object Question : ManagedTable("questions") {
    /**
     * Placeholder for frontend.
     */
    val placeholder = varchar("placeholder", DatabaseTypeLength.DEFAULT_STRING)

    /**
     * Czech translation.
     */
    val cs = text("cs")

    /**
     * English translation.
     */
    val eng = text("eng")
}
