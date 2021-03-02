package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.Patient
import blue.mild.covid.vaxx.dto.NewPatientDto
import blue.mild.covid.vaxx.dto.PatientDto
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pw.forst.tools.katlib.toUuid
import java.util.UUID

class PatientService {
    suspend fun getAllPatients(): List<PatientDto> = newSuspendedTransaction {
        Patient.selectAll().map {
            PatientDto(
                id = it[Patient.id].toUuid(),
                firstName = it[Patient.firstName],
                lastName = it[Patient.lastName],
                personalNumber = it[Patient.personalNumber],
                phoneNumber = it[Patient.phoneNumber],
                email = it[Patient.email]
            )
        }
    }

    suspend fun savePatient(patientDto: NewPatientDto) = newSuspendedTransaction {
        Patient.insert {
            it[id] = UUID.randomUUID().toString()
            it[firstName] = patientDto.firstName
            it[lastName] = patientDto.lastName
            it[personalNumber] = patientDto.personalNumber
            it[phoneNumber] = patientDto.phoneNumber
            it[email] = patientDto.email
        }
    }
}
