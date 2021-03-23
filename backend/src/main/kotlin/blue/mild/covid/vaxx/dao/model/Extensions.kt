package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import java.util.UUID

/**
 * Represents format for IDs.
 */
typealias EntityId = UUID

/**
 * Creates ID column.
 */
fun Table.entityId(name: String): Column<EntityId> = uuid(name)

/**
 * Reference to [Nurse.id].
 */
fun Table.nurseReference(name: String = "nurse_id"): Column<EntityId> = entityId(name) references Nurse.id

/**
 * Reference to [User.id].
 */
fun Table.userReference(name: String = "user_id"): Column<EntityId> = entityId(name) references User.id

/**
 * Reference to [Patient.id].
 */
fun Table.patientReference(name: String = "patient_id"): Column<EntityId> = entityId(name) references Patient.id

/**
 * Reference to [Question.id].
 */
fun Table.questionReference(name: String = "question_id"): Column<EntityId> = entityId(name) references Question.id
