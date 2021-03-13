package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp
import java.time.Instant

object Answer : Table("answers") {
    val created: Column<Instant> = timestamp("created")
    val updated: Column<Instant> = timestamp("updated")

    val questionId = varchar("question_id", DatabaseTypeLength.ID) references Question.id
    val patientId = varchar("patient_id", DatabaseTypeLength.ID) references Patient.id

    val value = bool("value")

    override val primaryKey = PrimaryKey(questionId, patientId)
}
