package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Nurse
import blue.mild.covid.vaxx.dao.model.User
import blue.mild.covid.vaxx.dao.model.Vaccination
import blue.mild.covid.vaxx.dao.model.VaccinationBodyPart
import blue.mild.covid.vaxx.dto.response.PersonnelDtoOut
import blue.mild.covid.vaxx.dto.response.VaccinationDetailDtoOut
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

class VaccinationRepository {
    /**
     * Creates new vaccination record for given data.
     */
    suspend fun addVaccination(
        patientId: EntityId,
        bodyPart: VaccinationBodyPart,
        vaccinatedOn: Instant,
        vaccineSerialNumber: String,
        userPerformingVaccination: EntityId,
        nurseId: EntityId? = null,
        notes: String? = null
    ): EntityId = newSuspendedTransaction {
        Vaccination.insert {
            it[Vaccination.patientId] = patientId
            it[Vaccination.bodyPart] = bodyPart
            it[Vaccination.vaccinatedOn] = vaccinatedOn
            it[Vaccination.vaccineSerialNumber] = vaccineSerialNumber
            it[Vaccination.userPerformingVaccination] = userPerformingVaccination
            it[Vaccination.nurseId] = nurseId
            it[Vaccination.notes] = notes
        }[Vaccination.id]
    }

    /**
     * Returns [VaccinationDetailDtoOut] if the patient was vaccinated.
     */
    suspend fun getForPatient(patientId: EntityId): VaccinationDetailDtoOut? =
        get { Vaccination.patientId eq patientId }

    /**
     * Returns [VaccinationDetailDtoOut] if the vaccination id is found.
     */
    suspend fun get(id: EntityId): VaccinationDetailDtoOut? =
        get { Vaccination.id eq id }

    private suspend fun get(where: SqlExpressionBuilder.() -> Op<Boolean>) =
        newSuspendedTransaction {
            Vaccination
                .leftJoin(User, { userPerformingVaccination }, { id })
                .leftJoin(Nurse, { Vaccination.nurseId }, { id })
                .select(where)
                .singleOrNull()
                ?.let {
                    VaccinationDetailDtoOut(
                        vaccinationId = it[Vaccination.id],
                        patientId = it[Vaccination.patientId],
                        bodyPart = it[Vaccination.bodyPart],
                        vaccinatedOn = it[Vaccination.vaccinatedOn],
                        vaccineSerialNumber = it[Vaccination.vaccineSerialNumber],
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
                        notes = it[Vaccination.notes]
                    )
                }
        }

}
