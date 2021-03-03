package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.Answer
import blue.mild.covid.vaxx.dao.Patient
import blue.mild.covid.vaxx.dto.AnswerDto
import blue.mild.covid.vaxx.dto.PatientDeletedDtoOut
import blue.mild.covid.vaxx.dto.PatientDtoOut
import blue.mild.covid.vaxx.dto.PatientRegistrationDtoIn
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

class PatientService {
    suspend fun getPatientById(patientId: UUID): PatientDtoOut = newSuspendedTransaction {
        val query = Patient
            .leftJoin(Answer, { id }, { Answer.patientId })
            .select { Patient.id eq patientId.toString() }
        val answers = query
            .map { AnswerDto(it[Answer.questionId].toUuid(), it[Answer.value]) }

        query
            .firstOrNull()
            ?.let {
                PatientDtoOut(
                    id = it[Patient.id].toUuid(),
                    firstName = it[Patient.firstName],
                    lastName = it[Patient.lastName],
                    personalNumber = it[Patient.personalNumber],
                    phoneNumber = it[Patient.phoneNumber],
                    email = it[Patient.email],
                    answers = answers
                )
            } ?: throw entityNotFound<Patient>(Patient::id, patientId)
    }

    suspend fun getAllPatients(): List<PatientDtoOut> =
        newSuspendedTransaction { getAndMapPatients() }

    suspend fun getPatientsByPersonalNumber(patientPersonalNumber: String): List<PatientDtoOut> =
        newSuspendedTransaction { getAndMapPatients { Patient.personalNumber eq patientPersonalNumber } }

    suspend fun getPatientsByEmail(email: String): List<PatientDtoOut> =
        newSuspendedTransaction { getAndMapPatients { Patient.email eq email } }

    suspend fun savePatient(patientDto: PatientRegistrationDtoIn) = newSuspendedTransaction {
        val patientId = UUID.randomUUID().toString()

        Patient.insert {
            it[id] = patientId
            it[firstName] = patientDto.firstName
            it[lastName] = patientDto.lastName
            it[personalNumber] = patientDto.personalNumber
            it[phoneNumber] = patientDto.phoneNumber
            it[email] = patientDto.email
        }

        Answer.batchInsert(patientDto.answers) {
            this[Answer.patientId] = patientId
            this[Answer.questionId] = it.questionId.toString()
            this[Answer.value] = it.value
        }
    }

    suspend fun deletePatientById(patientId: UUID) = newSuspendedTransaction {
        val count = Patient.deleteWhere { Patient.id eq patientId.toString() }
        if (count == 1) PatientDeletedDtoOut(true)
        else throw entityNotFound<Patient>(Patient::id, patientId)
    }

    private fun getAndMapPatients(where: (SqlExpressionBuilder.() -> Op<Boolean>)? = null) =
        Patient
            .leftJoin(Answer, { id }, { patientId })
            .let { if (where != null) it.select(where) else it.selectAll() }
            .let { query ->
                val answers = query
                    .groupBy({ it[Patient.id] }, { AnswerDto(it[Answer.questionId].toUuid(), it[Answer.value]) })

                query
                    .distinctBy { it[Patient.id] }
                    .map {
                        PatientDtoOut(
                            id = it[Patient.id].toUuid(),
                            firstName = it[Patient.firstName],
                            lastName = it[Patient.lastName],
                            personalNumber = it[Patient.personalNumber],
                            phoneNumber = it[Patient.phoneNumber],
                            email = it[Patient.email],
                            answers = answers.getValue(it[Patient.id])
                        )
                    }
            }
}
