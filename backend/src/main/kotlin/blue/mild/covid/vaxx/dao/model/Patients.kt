package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.`java-time`.timestamp

object Patients : ManagedTable("patients") {
    /**
     * First name.
     */
    val firstName = varchar("first_name", DatabaseTypeLength.DEFAULT_STRING)

    /**
     * Last name.
     */
    val lastName = varchar("last_name", DatabaseTypeLength.DEFAULT_STRING)

    /**
     * City zip code in form xxxyy.
     */
    val zipCode = integer("zip_code")

    /**
     * City district.
     */
    val district = varchar("district", DatabaseTypeLength.SHORT_STRING)

    /**
     * Personal/Birth number = Rodne Cislo.
     */
    val personalNumber = varchar("personal_number", DatabaseTypeLength.PERSONAL_NUMBER)

    /**
     * Validated phone number in format +420xxxyyyzzz
     */
    val phoneNumber = varchar("phone_number", DatabaseTypeLength.PHONE_NUMBER)

    /**
     * Validated email address.
     */
    val email = varchar("email", DatabaseTypeLength.DEFAULT_STRING)

    /**
     * Insurance company [InsuranceCompany].
     */
    val insuranceCompany = enumerationByName("insurance_company", DatabaseTypeLength.INSURANCE_COMPANY, InsuranceCompany::class)

    /**
     * Indication about patient - ie. chronic disease or teacher.
     */
    val indication = varchar("indication", DatabaseTypeLength.DEFAULT_STRING).nullable()

    /**
     * From which IP address was the registration created.
     */
    val remoteHost = varchar("remote_host", DatabaseTypeLength.REMOTE_HOST)

    /**
     * When the server sent the registration confirmation to the patient.
     */
    val registrationEmailSent = timestamp("email_sent_date").nullable()

    /**
     * Verification that the data are correct.
     */
    val dataCorrectness = (entityId("data_correctness_id") references PatientDataCorrectnessConfirmation.id).nullable()

    /**
     * Data about patient's vaccination.
     */
    val vaccination = (entityId("vaccination_id") references Vaccinations.id).nullable()

    /**
     * Patient was validated in isin
     */
    val isIsinValidated = bool("is_isin_validated")
}
