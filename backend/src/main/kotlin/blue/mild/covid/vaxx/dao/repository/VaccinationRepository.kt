package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Nurses
import blue.mild.covid.vaxx.dao.model.Patients
import blue.mild.covid.vaxx.dao.model.Users
import blue.mild.covid.vaxx.dao.model.VaccinationBodyPart
import blue.mild.covid.vaxx.dao.model.Vaccinations
import blue.mild.covid.vaxx.dto.response.PersonnelDtoOut
import blue.mild.covid.vaxx.dto.response.VaccinationDetailDtoOut
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.LocalDate

@Suppress("LongParameterList") // it's a repository, we're fine with this
class VaccinationRepository {
    /**
     * Creates new vaccination record for given data.
     */
    suspend fun addVaccination(
        patientId: EntityId,
        bodyPart: VaccinationBodyPart,
        vaccinatedOn: Instant,
        vaccineSerialNumber: String,
        vaccineExpiration: LocalDate,
        userPerformingVaccination: EntityId,
        doseNumber: Int,
        nurseId: EntityId? = null,
        notes: String? = null
    ): EntityId = newSuspendedTransaction {
        if (doseNumber != 1 && doseNumber != 2) {
            throw IllegalArgumentException("Dose number was $doseNumber which is not valid.")
        }

        val id = Vaccinations.insert {
            it[Vaccinations.patientId] = patientId
            it[Vaccinations.bodyPart] = bodyPart
            it[Vaccinations.vaccinatedOn] = vaccinatedOn
            it[Vaccinations.vaccineSerialNumber] = vaccineSerialNumber
            it[Vaccinations.vaccineExpiration] = vaccineExpiration
            it[Vaccinations.userPerformingVaccination] = userPerformingVaccination
            it[Vaccinations.doseNumber] = doseNumber
            it[Vaccinations.nurseId] = nurseId
            it[Vaccinations.notes] = notes
        }[Vaccinations.id]

        // now set backref
        if (doseNumber == 1) {
            Patients.update(
                where = { Patients.id eq patientId },
                body = { it[vaccination] = id }
            )
        } else {
            Patients.update(
                where = { Patients.id eq patientId },
                body = { it[vaccinationSecondDose] = id }
            )
        }
        id
    }

    /**
     * Updates vaccination entity with id [vaccinationId].
     */
    suspend fun updateVaccination(
        vaccinationId: EntityId,
        bodyPart: VaccinationBodyPart? = null,
        vaccinatedOn: Instant? = null,
        vaccineSerialNumber: String? = null,
        vaccineExpiration: LocalDate? = null,
        userPerformingVaccination: EntityId? = null,
        nurseId: EntityId? = null,
        notes: String? = null,
        exportedToIsinOn: Instant? = null
    ): Boolean = newSuspendedTransaction {
        val isUpdateNecessary =
            bodyPart ?: vaccinatedOn
            ?: vaccineSerialNumber ?: vaccineExpiration ?: userPerformingVaccination
            ?: nurseId ?: notes ?: exportedToIsinOn

        // if so, perform update query
        val vaccinationUpdated = if (isUpdateNecessary != null) {
            Vaccinations.update(
                where = { Vaccinations.id eq vaccinationId },
                body = { row ->
                    row.apply {
                        updateIfNotNull(bodyPart, Vaccinations.bodyPart)
                        updateIfNotNull(vaccinatedOn, Vaccinations.vaccinatedOn)
                        updateIfNotNull(vaccineSerialNumber, Vaccinations.vaccineSerialNumber)
                        updateIfNotNull(vaccineExpiration, Vaccinations.vaccineExpiration)
                        updateIfNotNull(userPerformingVaccination, Vaccinations.userPerformingVaccination)
                        updateIfNotNull(nurseId, Vaccinations.nurseId)
                        updateIfNotNull(notes, Vaccinations.notes)
                        updateIfNotNull(exportedToIsinOn, Vaccinations.exportedToIsinOn)
                    }
                }
            )
        } else 0
        vaccinationUpdated == 1
    }

    /**
     * Returns [VaccinationDetailDtoOut] if the patient was vaccinated.
     */
    suspend fun getForPatient(patientId: EntityId, doseNumber: Int): VaccinationDetailDtoOut? =
        get { (Vaccinations.patientId eq patientId) and (Vaccinations.doseNumber eq doseNumber) }

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
                        vaccineExpiration = it[Vaccinations.vaccineExpiration],
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
                        notes = it[Vaccinations.notes],
                        exportedToIsinOn = it[Vaccinations.exportedToIsinOn],
                        doseNumber = it[Vaccinations.doseNumber]
                    )
                }
        }
}
