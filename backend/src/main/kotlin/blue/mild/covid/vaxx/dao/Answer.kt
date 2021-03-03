package blue.mild.covid.vaxx.dao

import org.jetbrains.exposed.sql.Table

object Answer : Table("answers") {
    val questionId = varchar("question_id", 36) references Question.id
    val patientId = varchar("patient_id", 36) references Patient.id

    val value = bool("value")

    override val primaryKey = PrimaryKey(questionId, patientId)
}
