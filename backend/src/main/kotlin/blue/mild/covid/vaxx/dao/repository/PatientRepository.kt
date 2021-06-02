package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.Answers
import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dao.model.PatientDataCorrectnessConfirmation
import blue.mild.covid.vaxx.dao.model.Patients
import blue.mild.covid.vaxx.dao.model.VaccinationSlots
import blue.mild.covid.vaxx.dao.model.Vaccinations
import blue.mild.covid.vaxx.dto.response.AnswerDtoOut
import blue.mild.covid.vaxx.dto.response.DataCorrectnessConfirmationDtoOut
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import blue.mild.covid.vaxx.dto.response.VaccinationDtoOut
import blue.mild.covid.vaxx.dto.response.VaccinationSlotDtoOut
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import pw.forst.katlib.TimeProvider
import java.time.Instant

@Suppress("LongParameterList") // it's a repository, we're fine with this
class PatientRepository(
    private val instantTimeProvider: TimeProvider<Instant>
) {

    /**
     * Updates patient entity with given id. Change set of the given properties
     * is applied to the entity.
     *
     * Note: use named parameters while using this method.
     */
    suspend fun updatePatientChangeSet(
        id: EntityId,
        firstName: String? = null,
        lastName: String? = null,
        zipCode: Int? = null,
        district: String? = null,
        phoneNumber: String? = null,
        personalNumber: String? = null,
        email: String? = null,
        insuranceCompany: InsuranceCompany? = null,
        indication: String? = null,
        answers: Map<EntityId, Boolean>? = null,
        registrationEmailSent: Instant? = null
    ): Boolean = newSuspendedTransaction {
        // check if any property is not null
        val isPatientEntityUpdateNecessary =
            firstName ?: lastName
            ?: district ?: zipCode
            ?: phoneNumber ?: personalNumber ?: email
            ?: insuranceCompany ?: registrationEmailSent ?: indication
        // if so, perform update query
        val patientUpdated = if (isPatientEntityUpdateNecessary != null) {
            Patients.update(
                where = { Patients.id eq id },
                body = { row ->
                    row.apply {
                        updateIfNotNull(firstName, Patients.firstName)
                        updateIfNotNull(lastName, Patients.lastName)
                        updateIfNotNull(zipCode, Patients.zipCode)
                        updateIfNotNull(district, Patients.district)
                        updateIfNotNull(phoneNumber, Patients.phoneNumber)
                        updateIfNotNull(personalNumber, Patients.personalNumber)
                        updateIfNotNull(email, Patients.email)
                        updateIfNotNull(insuranceCompany, Patients.insuranceCompany)
                        updateIfNotNull(indication, Patients.indication)
                        updateIfNotNull(registrationEmailSent, Patients.registrationEmailSent)
                    }
                }
            )
        } else 0
        // check if it is necessary to update answers
        val answersUpdated = if (answers != null && answers.isNotEmpty()) {
            answers.entries
                .groupBy({ it.value }, { it.key })
                .map { (value, questionIds) ->
                    Answers.update(
                        where = { (Answers.patientId eq id) and (Answers.questionId inList questionIds) },
                        body = { row -> row[Answers.value] = value }
                    )
                }.sum()
        } else 0
        // indicate that patient was updated if patient entity was updated OR at least one answer was updated
        patientUpdated + answersUpdated >= 1
    }

    /**
     * Gets and maps patients by given where clause.
     *
     * If no clause is given, it returns and maps whole database.
     */
    suspend fun getAndMapPatientsBy(
        where: (SqlExpressionBuilder.() -> Op<Boolean>)? = null
    ): List<PatientDtoOut> = newSuspendedTransaction { getAndMapPatients(where) }

    /**
     * Saves the given data to the database as a new patient registration record.
     *
     * Note: use named parameters while using this method.
     */
    // this can't be refactored as it is builder
    @Suppress("LongParameterList")
    suspend fun savePatient(
        firstName: String,
        lastName: String,
        zipCode: Int,
        district: String,
        phoneNumber: String,
        personalNumber: String,
        email: String,
        insuranceCompany: InsuranceCompany,
        indication: String?,
        remoteHost: String,
        answers: Map<EntityId, Boolean>
    ): EntityId = newSuspendedTransaction {
        val patientId = Patients.insert {
            it[Patients.firstName] = firstName
            it[Patients.lastName] = lastName
            it[Patients.zipCode] = zipCode
            it[Patients.district] = district
            it[Patients.personalNumber] = personalNumber
            it[Patients.phoneNumber] = phoneNumber
            it[Patients.email] = email
            it[Patients.insuranceCompany] = insuranceCompany
            it[Patients.indication] = indication
            it[Patients.remoteHost] = remoteHost
        }[Patients.id]

        val now = instantTimeProvider.now()
        val answersIterable = answers.map { (questionId, value) -> questionId to value }
        // even though this statement prints multiple insert into statements
        // they are in a fact translated to one thanks to reWriteBatchedInserts=true
        // see https://github.com/JetBrains/Exposed/wiki/DSL#batch-insert
        Answers.batchInsert(answersIterable, shouldReturnGeneratedValues = false) { (questionId, value) ->
            // we want to specify these values because DB defaults don't support in batch inserts
            // however, the real value will be set by the database once inserted
            this[Answers.created] = now
            this[Answers.updated] = now

            this[Answers.patientId] = patientId
            this[Answers.questionId] = questionId
            this[Answers.value] = value
        }
        patientId
    }

    /**
     * Deletes patient entity by given [where]. Returns number of deleted entities.
     */
    suspend fun deletePatientsBy(where: (SqlExpressionBuilder.() -> Op<Boolean>)): Int =
        newSuspendedTransaction { Patients.deleteWhere(op = where) }


    private fun getAndMapPatients(where: (SqlExpressionBuilder.() -> Op<Boolean>)? = null) =
        Patients
            .leftJoin(Answers, { id }, { patientId })
            .leftJoin(Vaccinations, { Patients.vaccination }, { id })
            .leftJoin(PatientDataCorrectnessConfirmation, { Patients.dataCorrectness }, { id })
            .leftJoin(VaccinationSlots, { Patients.id }, { patientId })
            .let { if (where != null) it.select(where) else it.selectAll() }
            .toList() // eager fetch all data from the database
            .let { data ->
                val answers = data.groupBy({ it[Patients.id] }, { it.mapAnswer() })
                data.distinctBy { it[Patients.id] }
                    .map { mapPatient(it, answers.getValue(it[Patients.id])) }
            }

    private fun mapPatient(row: ResultRow, answers: List<AnswerDtoOut>) = PatientDtoOut(
        id = row[Patients.id],
        firstName = row[Patients.firstName],
        lastName = row[Patients.lastName],
        zipCode = row[Patients.zipCode],
        district = row[Patients.district],
        personalNumber = row[Patients.personalNumber],
        phoneNumber = row[Patients.phoneNumber],
        email = row[Patients.email],
        insuranceCompany = row[Patients.insuranceCompany],
        indication = row[Patients.indication],
        registrationEmailSentOn = row[Patients.registrationEmailSent],
        answers = answers,
        registeredOn = row[Patients.created],
        vaccinated = row.mapVaccinated(),
        dataCorrect = row.mapDataCorrect(),
        vaccinationSlotDtoOut = row.mapVaccinationSlot()
    )

    private fun ResultRow.mapVaccinationSlot() = getOrNull(VaccinationSlots.id)?.let {
        VaccinationSlotDtoOut(
            id = this[VaccinationSlots.id],
            locationId = this[VaccinationSlots.locationId],
            patientId = this[VaccinationSlots.patientId],
            queue = this[VaccinationSlots.queue],
            from = this[VaccinationSlots.from],
            to = this[VaccinationSlots.to]
        )
    }

    private fun ResultRow.mapDataCorrect() = getOrNull(PatientDataCorrectnessConfirmation.id)?.let {
        DataCorrectnessConfirmationDtoOut(
            id = this[PatientDataCorrectnessConfirmation.id],
            dataAreCorrect = this[PatientDataCorrectnessConfirmation.dataAreCorrect]
        )
    }

    private fun ResultRow.mapVaccinated() = getOrNull(Vaccinations.id)?.let {
        VaccinationDtoOut(
            id = this[Vaccinations.id],
            vaccinatedOn = this[Vaccinations.vaccinatedOn]
        )
    }

    private fun ResultRow.mapAnswer() = AnswerDtoOut(
        questionId = this[Answers.questionId],
        value = this[Answers.value]
    )
}
