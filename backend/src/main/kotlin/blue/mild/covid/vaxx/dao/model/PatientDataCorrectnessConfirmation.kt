package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp

object PatientDataCorrectnessConfirmation : Table("patient_data_correctness") {
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
     * Patient data that were checked.
     */
    val patientId = entityId("patient_id") references Patient.id

    /**
     * What user performed check.
     */
    val userPerformedCheck = entityId("user_performed_check") references User.id

    /**
     * Nurse set during the login.
     */
    val nurseId = entityId("nurse_id").nullable()

    /**
     * Indication that the data are correct.
     */
    val dataAreCorrect = bool("data_are_correct")

    /**
     * Optional notes for the data check.
     */
    val notes = text("notes").nullable()

    override val primaryKey = PrimaryKey(id)
}
