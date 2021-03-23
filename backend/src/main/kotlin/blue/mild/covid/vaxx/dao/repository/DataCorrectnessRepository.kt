package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Nurse
import blue.mild.covid.vaxx.dao.model.PatientDataCorrectnessConfirmation
import blue.mild.covid.vaxx.dao.model.User
import blue.mild.covid.vaxx.dto.response.DataCorrectnessConfirmationDetailDtoOut
import blue.mild.covid.vaxx.dto.response.PersonnelDtoOut
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class DataCorrectnessRepository {
    /**
     * Creates new data correctness record.
     */
    suspend fun registerCorrectness(
        patientId: EntityId,
        userPerformedCheck: EntityId,
        nurseId: EntityId?,
        dataAreCorrect: Boolean,
        notes: String? = null
    ): EntityId = newSuspendedTransaction {
        PatientDataCorrectnessConfirmation.insert {
            it[PatientDataCorrectnessConfirmation.patientId] = patientId
            it[PatientDataCorrectnessConfirmation.userPerformedCheck] = userPerformedCheck
            it[PatientDataCorrectnessConfirmation.nurseId] = nurseId
            it[PatientDataCorrectnessConfirmation.dataAreCorrect] = dataAreCorrect
            it[PatientDataCorrectnessConfirmation.notes] = notes
        }[PatientDataCorrectnessConfirmation.id]
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
                .leftJoin(User, { userPerformedCheck }, { id })
                .leftJoin(Nurse, { PatientDataCorrectnessConfirmation.nurseId }, { id })
                .select(where)
                .singleOrNull()
                ?.let {
                    DataCorrectnessConfirmationDetailDtoOut(
                        id = it[PatientDataCorrectnessConfirmation.id],
                        checked = it[PatientDataCorrectnessConfirmation.created],
                        dataAreCorrect = it[PatientDataCorrectnessConfirmation.dataAreCorrect],
                        notes = it[PatientDataCorrectnessConfirmation.notes],
                        doctor = PersonnelDtoOut(
                            id = it[User.id],
                            firstName = it[User.firstName],
                            lastName = it[User.lastName],
                            email = it[User.email]
                        ),
                        nurse = if (it.hasValue(Nurse.id)) PersonnelDtoOut(
                            id = it[Nurse.id],
                            firstName = it[Nurse.firstName],
                            lastName = it[Nurse.lastName],
                            email = it[Nurse.email]
                        ) else null,
                    )
                }
        }

}
