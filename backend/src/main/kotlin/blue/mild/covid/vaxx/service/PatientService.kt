package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.Answer
import blue.mild.covid.vaxx.dao.Patient
import blue.mild.covid.vaxx.dto.AnswerDto
import blue.mild.covid.vaxx.dto.PatientRegistrationDto
import blue.mild.covid.vaxx.dto.response.PatientDeletedDtoOut
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import blue.mild.covid.vaxx.dto.response.PatientRegisteredDtoOut
import blue.mild.covid.vaxx.error.entityNotFound
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pw.forst.tools.katlib.toUuid
import java.util.UUID

class PatientService(
    private val validationService: ValidationService,
    private val instantTimeProvider: InstantTimeProvider,
    private val entityIdProvider: EntityIdProvider
) {

    suspend fun getPatientById(patientId: UUID): PatientDtoOut = newSuspendedTransaction {
        val data = Patient
            .leftJoin(Answer, { id }, { Answer.patientId })
            .select { Patient.id eq patientId.toString() }
            .toList() // eager fetch all data from the database

        val answers = data.map { AnswerDto(it[Answer.questionId].toUuid(), it[Answer.value]) }

        data.firstOrNull()?.let {
            PatientDtoOut(
                id = it[Patient.id].toUuid(),
                firstName = it[Patient.firstName],
                lastName = it[Patient.lastName],
                personalNumber = it[Patient.personalNumber],
                phoneNumber = it[Patient.phoneNumber],
                email = it[Patient.email],
                insuranceCompany = it[Patient.insuranceCompany],
                answers = answers,
                created = it[Patient.created],
                updated = it[Patient.updated]
            )
        } ?: throw entityNotFound<Patient>(Patient::id, patientId)
    }

    suspend fun getAllPatients(): List<PatientDtoOut> =
        newSuspendedTransaction { getAndMapPatients() }

    suspend fun getPatientsByPersonalNumber(patientPersonalNumber: String): List<PatientDtoOut> {
        val normalizedPersonalNumber = normalizePersonalNumber(patientPersonalNumber)
        return newSuspendedTransaction { getAndMapPatients { Patient.personalNumber eq normalizedPersonalNumber } }
    }

    suspend fun getPatientsByEmail(email: String): List<PatientDtoOut> =
        newSuspendedTransaction { getAndMapPatients { Patient.email eq email } }

    suspend fun savePatient(patientRegistrationDto: PatientRegistrationDto) = newSuspendedTransaction {
        val (registration, registrationRemoteHost) = patientRegistrationDto
        validationService.validatePatientRegistrationAndThrow(registration)

        val (entityId, stringId) = entityIdProvider.generateId()

        Patient.insert {
            it[id] = stringId
            it[firstName] = registration.firstName
            it[lastName] = registration.lastName
            it[personalNumber] = normalizePersonalNumber(registration.personalNumber)
            it[phoneNumber] = registration.phoneNumber
            it[email] = registration.email
            it[insuranceCompany] = registration.insuranceCompany
            it[remoteHost] = registrationRemoteHost
        }

        val now = instantTimeProvider.now()
        // even though this statement prints multiple insert into statements
        // they are in a fact translated to one thanks to reWriteBatchedInserts=true
        // see https://github.com/JetBrains/Exposed/wiki/DSL#batch-insert
        Answer.batchInsert(patientRegistrationDto.answers, shouldReturnGeneratedValues = false) {
            // we want to specify these values because DB defaults don't support in batch inserts
            // however, the real value will be set by the database once inserted
            this[Answer.created] = now
            this[Answer.updated] = now

            this[Answer.patientId] = stringId
            this[Answer.questionId] = it.questionId.toString()
            this[Answer.value] = it.value
        }

        PatientCreatedDtoOut(entityId)
    }

    suspend fun deletePatientById(patientId: UUID) = newSuspendedTransaction {
        val count = Patient.deleteWhere { Patient.id eq patientId.toString() }
        if (count == 1) PatientDeletedDtoOut(true)
        else throw entityNotFound<Patient>(Patient::id, patientId)
    }

    private fun normalizePersonalNumber(personalNumber: String): String = personalNumber.replace("/", "")

    private fun getAndMapPatients(where: (SqlExpressionBuilder.() -> Op<Boolean>)? = null) =
        Patient
            .leftJoin(Answer, { id }, { patientId })
            .let { if (where != null) it.select(where) else it.selectAll() }
            .toList() // eager fetch all data from the database
            .let { data ->
                val answers = data.groupBy(
                    { it[Patient.id] },
                    { AnswerDto(it[Answer.questionId].toUuid(), it[Answer.value]) }
                )
                data.distinctBy { it[Patient.id] }
                    .map {
                        PatientDtoOut(
                            id = it[Patient.id].toUuid(),
                            firstName = it[Patient.firstName],
                            lastName = it[Patient.lastName],
                            personalNumber = it[Patient.personalNumber],
                            phoneNumber = it[Patient.phoneNumber],
                            email = it[Patient.email],
                            insuranceCompany = it[Patient.insuranceCompany],
                            answers = answers.getValue(it[Patient.id]),
                            created = it[Patient.created],
                            updated = it[Patient.updated]
                        )
                    }
            }
}
