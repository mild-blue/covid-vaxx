package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Nurses
import blue.mild.covid.vaxx.dao.model.Users
import blue.mild.covid.vaxx.dao.model.VaccinationBodyPart
import blue.mild.covid.vaxx.dao.model.Vaccinations
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
        Vaccinations.insert {
            it[Vaccinations.patientId] = patientId
            it[Vaccinations.bodyPart] = bodyPart
            it[Vaccinations.vaccinatedOn] = vaccinatedOn
            it[Vaccinations.vaccineSerialNumber] = vaccineSerialNumber
            it[Vaccinations.userPerformingVaccination] = userPerformingVaccination
            it[Vaccinations.nurseId] = nurseId
            it[Vaccinations.notes] = notes
        }[Vaccinations.id]
    }

    /**
     * Returns [VaccinationDetailDtoOut] if the patient was vaccinated.
     */
    suspend fun getForPatient(patientId: EntityId): VaccinationDetailDtoOut? =
        get { Vaccinations.patientId eq patientId }

    /**
     * Returns [VaccinationDetailDtoOut] if the vaccination id is found.
     */
    suspend fun get(id: EntityId): VaccinationDetailDtoOut? =
        get { Vaccinations.id eq id }

    private suspend fun get(where: SqlExpressionBuilder.() -> Op<Boolean>) =
        newSuspendedTransaction {
            Vaccinations
                .leftJoin(Users, { userPerformingVaccination }, { id })
                .leftJoin(Nurses, { Vaccinations.nurseId }, { id })
                .select(where)
                .singleOrNull()
                ?.let {
                    VaccinationDetailDtoOut(
                        vaccinationId = it[Vaccinations.id],
                        patientId = it[Vaccinations.patientId],
                        bodyPart = it[Vaccinations.bodyPart],
                        vaccinatedOn = it[Vaccinations.vaccinatedOn],
                        vaccineSerialNumber = it[Vaccinations.vaccineSerialNumber],
                        doctor = PersonnelDtoOut(
                            id = it[Users.id],
                            firstName = it[Users.firstName],
                            lastName = it[Users.lastName],
                            email = it[Users.email]
                        ),
                        nurse = if (it.hasValue(Nurses.id)) PersonnelDtoOut(
                            id = it[Nurses.id],
                            firstName = it[Nurses.firstName],
                            lastName = it[Nurses.lastName],
                            email = it[Nurses.email]
                        ) else null,
                        notes = it[Vaccinations.notes]
                    )
                }
        }

}
