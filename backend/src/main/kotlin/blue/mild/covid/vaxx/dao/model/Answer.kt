package blue.mild.covid.vaxx.dao.model

import blue.mild.covid.vaxx.dao.model.Answer.patientId
import blue.mild.covid.vaxx.dao.model.Answer.questionId
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp

/**
 * Answers to the [questionId] for given [patientId].
 */
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
    val questionId = questionReference()

    /**
     * Answered by [patientId].
     */
    val patientId = patientReference()

    /**
     * Value of the answer.
     */
    val value = bool("value")

    override val primaryKey = PrimaryKey(questionId, patientId)
}
