package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

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
     * Vaccine expiration date set during login.
     *
     * It is nullable because of the migration but it should always be set in newer versions.
     */
    val vaccineExpiration = date("vaccine_expiration").nullable()

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
    val exportedToIsinOn = timestamp("exported_to_isin_on").nullable()

    /**
     * 1 - first dose, 2 - second dose.
     */
    val doseNumber = integer("dose_number")
}
