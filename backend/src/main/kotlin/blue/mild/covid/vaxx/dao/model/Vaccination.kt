package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp

object Vaccination : Table("vaccinations") {
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
     * Who was vaccinated.
     */
    val patientId = entityId("patient_id") references Patient.id

    /**
     * To which body part was [patientId] vaccinated.
     */
    val vaccinationBodyPart = enumerationByName("body_part", DatabaseTypeLength.BODY_PART, VaccinationBodyPart::class)

    /**
     * Vaccine serial number set during login.
     */
    val vaccineSerialNumber = varchar("vaccine_serial_number", DatabaseTypeLength.DEFAULT_STRING)

    /**
     * What user administered vaccine.
     */
    val userPerformingVaccination = entityId("user_performing") references User.id

    /**
     * Nurse name set during the login.
     */
    val nurseName = varchar("nurse", DatabaseTypeLength.DEFAULT_STRING).nullable()

    /**
     * Optional notes for the performed vaccination.
     */
    val notes = text("notes").nullable()

    override val primaryKey = PrimaryKey(id)
}
