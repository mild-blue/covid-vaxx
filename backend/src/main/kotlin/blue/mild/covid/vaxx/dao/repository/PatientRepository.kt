package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.Answer
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
import pw.forst.tools.katlib.toUuid
import java.time.Instant
import java.util.UUID

class PatientRepository(
    private val instantTimeProvider: TimeProvider<Instant>,
) {

    /**
     * Updates patient entity with given id. Change set of the given properties
     * is applied to the entity.
     */
    suspend fun updatePatientChangeSet(
        id: UUID,
        firstName: String? = null,
        lastName: String? = null,
        phoneNumber: String? = null,
        personalNumber: String? = null,
        email: String? = null,
        insuranceCompany: InsuranceCompany? = null,
        answers: Map<UUID, Boolean>? = null,
        registrationEmailSent: Instant? = null,
        vaccinatedOn: Instant? = null
    ): Boolean = newSuspendedTransaction {
        val patientId = id.toString()
        val updated = Patient.update(
            where = { Patient.id eq patientId },
            body = { row ->
                row.apply {
                    updateIfNotNull(firstName, Patient.firstName)
                    updateIfNotNull(lastName, Patient.lastName)
                    updateIfNotNull(phoneNumber, Patient.phoneNumber)
                    updateIfNotNull(personalNumber, Patient.personalNumber)
                    updateIfNotNull(email, Patient.email)
                    updateIfNotNull(insuranceCompany, Patient.insuranceCompany)
                    updateIfNotNull(firstName, Patient.firstName)
                    updateIfNotNull(registrationEmailSent, Patient.registrationEmailSent)
                    updateIfNotNull(vaccinatedOn, Patient.vaccinatedOn)
                }
            }
        )
        // continue in update only if there's a patient with given ID
        // and answers needs to be updated as well
        if (updated == 1 && answers != null) {
            answers.forEach { (questionId, value) ->
                Answer.update(
                    where = { (Answer.questionId eq questionId.toString()) and (Answer.patientId eq patientId) },
                    body = { row -> row[Answer.value] = value }
                )
            }
        }
        // indicate that patient was updated if single patient row was updated
        updated == 1
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
     * Saves the given data to the database3 as a new patient registration record.
     *
     * Returns patient [id].
     */
    suspend fun savePatient(
        id: UUID,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        personalNumber: String,
        email: String,
        insuranceCompany: InsuranceCompany,
        remoteHost: String,
        answers: Map<UUID, Boolean>
    ): UUID = newSuspendedTransaction {
        val patientStringId = id.toString()
        Patient.insert {
            it[Patient.id] = patientStringId
            it[Patient.firstName] = firstName
            it[Patient.lastName] = lastName
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

            this[Answer.patientId] = patientStringId
            this[Answer.questionId] = questionId.toString()
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
        id = row[Patient.id].toUuid(),
        firstName = row[Patient.firstName],
        lastName = row[Patient.lastName],
        personalNumber = row[Patient.personalNumber],
        phoneNumber = row[Patient.phoneNumber],
        email = row[Patient.email],
        insuranceCompany = row[Patient.insuranceCompany],
        registrationEmailSentOn = row[Patient.registrationEmailSent],
        vaccinatedOn = row[Patient.vaccinatedOn],
        answers = answers,
        created = row[Patient.created],
        updated = row[Patient.updated]
    )

    private fun mapAnswer(row: ResultRow) = AnswerDto(
        questionId = row[Answer.questionId].toUuid(),
        value = row[Answer.value]
    )
}
