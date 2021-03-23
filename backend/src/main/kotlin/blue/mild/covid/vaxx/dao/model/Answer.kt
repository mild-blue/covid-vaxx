package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp

object Answer : Table("answers") {
    /**
     * When this record was created.
     */
    val created = timestamp("created")

    /**
     * When this record was updated. By default same value as [created].
     */
    val updated = timestamp("updated")

    /**
     * This answer is for the [questionId].
     */
    val questionId = entityId("question_id") references Question.id

    /**
     * Answered by [patientId].
     */
    val patientId = entityId("patient_id") references Patient.id

    /**
     * Value of the answer.
     */
    val value = bool("value")

    override val primaryKey = PrimaryKey(questionId, patientId)
}
