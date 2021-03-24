package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.`java-time`.timestamp

/**
 * Administered vaccination of the [Patients].
 */
object Vaccinations : ManagedTable("vaccinations") {
    /**
     * Who was vaccinated.
     */
    val patientId = patientReference()

    /**
     * To which body part was [patientId] vaccinated.
     */
    val bodyPart = enumerationByName("body_part", DatabaseTypeLength.BODY_PART, VaccinationBodyPart::class)

    /**
     * When was the vaccination administrated.
     */
    val vaccinatedOn = timestamp("vaccinated_on")

    /**
     * Vaccine serial number set during login.
     */
    val vaccineSerialNumber = varchar("vaccine_serial_number", DatabaseTypeLength.DEFAULT_STRING)

    /**
     * What user administered vaccine.
     */
    val userPerformingVaccination = userReference("user_performing_vaccination")

    /**
     * Nurse set during the login.
     */
    val nurseId = nurseReference().nullable()

    /**
     * Optional notes for the performed vaccination.
     */
    val notes = text("notes").nullable()

    /**
     * When was the vaccination exported to ISIN.
     */
    val exportedToIsinOn = timestamp("exported_to_isin_on")
}
