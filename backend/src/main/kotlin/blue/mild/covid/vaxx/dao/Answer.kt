package blue.mild.covid.vaxx.dao

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp
import java.time.Instant

object Answer : Table("answers") {
    val created: Column<Instant> = timestamp("created")
    val updated: Column<Instant?> = timestamp("updated").nullable()

    val questionId = varchar("question_id", 36) references Question.id
    val patientId = varchar("patient_id", 36) references Patient.id

    val value = bool("value")

    override val primaryKey = PrimaryKey(questionId, patientId)
}
