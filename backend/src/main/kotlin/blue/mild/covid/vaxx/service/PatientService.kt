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
                name = it[Patient.name]
            )
        }
    }

    suspend fun savePatient(patientDto: NewPatientDto) = newSuspendedTransaction {
        Patient.insert {
            it[id] = UUID.randomUUID().toString()
            it[name] = patientDto.name
        }
    }
}
