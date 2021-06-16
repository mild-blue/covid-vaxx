package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Nurses
import blue.mild.covid.vaxx.dao.model.PatientDataCorrectnessConfirmation
import blue.mild.covid.vaxx.dao.model.Patients
import blue.mild.covid.vaxx.dao.model.Users
import blue.mild.covid.vaxx.dto.response.DataCorrectnessConfirmationDetailDtoOut
import blue.mild.covid.vaxx.dto.response.PersonnelDtoOut
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

@Suppress("LongParameterList") // it's a repository, we're fine with this
class DataCorrectnessRepository {

    /**
     * Creates new data correctness record.
     */
    suspend fun registerCorrectness(
        patientId: EntityId,
        userPerformedCheck: EntityId,
        nurseId: EntityId?,
        dataAreCorrect: Boolean,
        notes: String? = null,
        exportedToIsinOn: Instant? = null
    ): EntityId = newSuspendedTransaction {
        val id = PatientDataCorrectnessConfirmation.insert {
            it[PatientDataCorrectnessConfirmation.patientId] = patientId
            it[PatientDataCorrectnessConfirmation.userPerformedCheck] = userPerformedCheck
            it[PatientDataCorrectnessConfirmation.nurseId] = nurseId
            it[PatientDataCorrectnessConfirmation.dataAreCorrect] = dataAreCorrect
            it[PatientDataCorrectnessConfirmation.notes] = notes
            it[PatientDataCorrectnessConfirmation.exportedToIsinOn] = exportedToIsinOn
        }[PatientDataCorrectnessConfirmation.id]
        // now set backref
        Patients.update(
            where = { Patients.id eq patientId },
            body = { it[dataCorrectness] = id }
        )
        id
    }

    /**
     * Returns [DataCorrectnessConfirmationDetailDtoOut] if the patient's data was verified and confirmed'.
     */
    suspend fun getForPatient(patientId: EntityId): DataCorrectnessConfirmationDetailDtoOut? =
        get { PatientDataCorrectnessConfirmation.patientId eq patientId }

    /**
     * Returns [DataCorrectnessConfirmationDetailDtoOut] if the data correctness id is found.
     */
    suspend fun get(id: EntityId): DataCorrectnessConfirmationDetailDtoOut? =
        get { PatientDataCorrectnessConfirmation.id eq id }

    private suspend fun get(where: SqlExpressionBuilder.() -> Op<Boolean>) =
        newSuspendedTransaction {
            PatientDataCorrectnessConfirmation
                .leftJoin(Users, { userPerformedCheck }, { id })
                .leftJoin(Nurses, { PatientDataCorrectnessConfirmation.nurseId }, { id })
                .select(where)
                .singleOrNull()
                ?.let {
                    DataCorrectnessConfirmationDetailDtoOut(
                        id = it[PatientDataCorrectnessConfirmation.id],
                        patientId = it[PatientDataCorrectnessConfirmation.patientId],
                        checked = it[PatientDataCorrectnessConfirmation.created],
                        dataAreCorrect = it[PatientDataCorrectnessConfirmation.dataAreCorrect],
                        notes = it[PatientDataCorrectnessConfirmation.notes],
                        doctor = PersonnelDtoOut(
                            id = it[Users.id],
                            firstName = it[Users.firstName],
                            lastName = it[Users.lastName],
                            email = it[Users.email]
                        ),
                        nurse = if (it.getOrNull(Nurses.id) != null) PersonnelDtoOut(
                            id = it[Nurses.id],
                            firstName = it[Nurses.firstName],
                            lastName = it[Nurses.lastName],
                            email = it[Nurses.email]
                        ) else null,
                        exportedToIsinOn = it[PatientDataCorrectnessConfirmation.exportedToIsinOn]
                    )
                }
        }


    /**
     * Updates correctness entity with id [correctnessId].
     */
    suspend fun updateCorrectness(
        correctnessId: EntityId,
        notes: String? = null,
        exportedToIsinOn: Instant? = null
    ): Boolean = newSuspendedTransaction {
        val isUpdateNecessary =
            notes ?: exportedToIsinOn

        // if so, perform update query
        val correctnessUpdated = if (isUpdateNecessary != null) {
            PatientDataCorrectnessConfirmation.update(
                where = { PatientDataCorrectnessConfirmation.id eq correctnessId },
                body = { row ->
                    row.apply {
                        updateIfNotNull(notes, PatientDataCorrectnessConfirmation.notes)
                        updateIfNotNull(exportedToIsinOn, PatientDataCorrectnessConfirmation.exportedToIsinOn)
                    }
                }
            )
        } else 0
        correctnessUpdated == 1
    }
}
