package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.Answer
import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dao.model.Patient
import blue.mild.covid.vaxx.dto.AnswerDto
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import org.jetbrains.exposed.sql.Column
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
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import pw.forst.tools.katlib.TimeProvider
import java.time.Instant

class PatientRepository(
    private val instantTimeProvider: TimeProvider<Instant>,
) {

    /**
     * Updates patient entity with given id. Change set of the given properties
     * is applied to the entity.
     *
     * Note: use named parameters while using this method.
     */
    // this can't be refactored as it is builder
    @Suppress("LongParameterList")
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
        answers: Map<EntityId, Boolean>? = null,
        registrationEmailSent: Instant? = null
    ): Boolean = newSuspendedTransaction {
        // check if any property is not null
        val isPatientEntityUpdateNecessary =
            firstName ?: lastName
            ?: district ?: zipCode
            ?: phoneNumber ?: personalNumber ?: email
            ?: insuranceCompany ?: registrationEmailSent
        // if so, perform update query
        val patientUpdated = if (isPatientEntityUpdateNecessary != null) {
            Patient.update(
                where = { Patient.id eq id },
                body = { row ->
                    row.apply {
                        updateIfNotNull(firstName, Patient.firstName)
                        updateIfNotNull(lastName, Patient.lastName)
                        updateIfNotNull(zipCode, Patient.zipCode)
                        updateIfNotNull(district, Patient.district)
                        updateIfNotNull(phoneNumber, Patient.phoneNumber)
                        updateIfNotNull(personalNumber, Patient.personalNumber)
                        updateIfNotNull(email, Patient.email)
                        updateIfNotNull(insuranceCompany, Patient.insuranceCompany)
                        updateIfNotNull(registrationEmailSent, Patient.registrationEmailSent)
                    }
                }
            )
        } else 0
        // check if it is necessary to update answers
        val answersUpdated = if (answers != null && answers.isNotEmpty()) {
            answers.entries
                .groupBy({ it.value }, { it.key })
                .map { (value, questionIds) ->
                    Answer.update(
                        where = { (Answer.patientId eq id) and (Answer.questionId inList questionIds) },
                        body = { row -> row[Answer.value] = value }
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
     * Returns patient [id].
     *
     * Note: use named parameters while using this method.
     */
    // this can't be refactored as it is builder
    @Suppress("LongParameterList")
    suspend fun savePatient(
        id: EntityId,
        firstName: String,
        lastName: String,
        zipCode: Int,
        district: String,
        phoneNumber: String,
        personalNumber: String,
        email: String,
        insuranceCompany: InsuranceCompany,
        remoteHost: String,
        answers: Map<EntityId, Boolean>
    ): EntityId = newSuspendedTransaction {
        Patient.insert {
            it[Patient.id] = id
            it[Patient.firstName] = firstName
            it[Patient.lastName] = lastName
            it[Patient.zipCode] = zipCode
            it[Patient.district] = district
            it[Patient.personalNumber] = personalNumber
            it[Patient.phoneNumber] = phoneNumber
            it[Patient.email] = email
            it[Patient.insuranceCompany] = insuranceCompany
            it[Patient.remoteHost] = remoteHost
        }

        val now = instantTimeProvider.now()
        val answersIterable = answers.map { (questionId, value) -> questionId to value }
        // even though this statement prints multiple insert into statements
        // they are in a fact translated to one thanks to reWriteBatchedInserts=true
        // see https://github.com/JetBrains/Exposed/wiki/DSL#batch-insert
        Answer.batchInsert(answersIterable, shouldReturnGeneratedValues = false) { (questionId, value) ->
            // we want to specify these values because DB defaults don't support in batch inserts
            // however, the real value will be set by the database once inserted
            this[Answer.created] = now
            this[Answer.updated] = now

            this[Answer.patientId] = id
            this[Answer.questionId] = questionId
            this[Answer.value] = value
        }
        id
    }

    /**
     * Deletes patient entity by given [where]. Returns number of deleted entities.
     */
    suspend fun deletePatientsBy(where: (SqlExpressionBuilder.() -> Op<Boolean>)): Int =
        newSuspendedTransaction { Patient.deleteWhere(op = where) }


    private fun <T> UpdateStatement.updateIfNotNull(value: T?, column: Column<T>) {
        if (value != null) {
            this[column] = value
        }
    }

    private fun getAndMapPatients(where: (SqlExpressionBuilder.() -> Op<Boolean>)? = null) =
        Patient
            .leftJoin(Answer, { id }, { patientId })
            .let { if (where != null) it.select(where) else it.selectAll() }
            .toList() // eager fetch all data from the database
            .let { data ->
                val answers = data.groupBy({ it[Patient.id] }, { mapAnswer(it) })
                data.distinctBy { it[Patient.id] }
                    .map { mapPatient(it, answers.getValue(it[Patient.id])) }
            }

    private fun mapPatient(row: ResultRow, answers: List<AnswerDto>) = PatientDtoOut(
        id = row[Patient.id],
        firstName = row[Patient.firstName],
        lastName = row[Patient.lastName],
        zipCode = row[Patient.zipCode],
        district = row[Patient.district],
        personalNumber = row[Patient.personalNumber],
        phoneNumber = row[Patient.phoneNumber],
        email = row[Patient.email],
        insuranceCompany = row[Patient.insuranceCompany],
        registrationEmailSentOn = row[Patient.registrationEmailSent],
        answers = answers,
        created = row[Patient.created],
        updated = row[Patient.updated]
    )

    private fun mapAnswer(row: ResultRow) = AnswerDto(
        questionId = row[Answer.questionId],
        value = row[Answer.value]
    )
}
