package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.VaccinationBodyPart
import blue.mild.covid.vaxx.dto.internal.PatientVaccinationDetailDto
import java.time.Instant

data class VaccinationDetailDtoOut(
    val vaccinationId: EntityId,
    val bodyPart: VaccinationBodyPart,
    val patientId: EntityId,
    val vaccinatedOn: Instant,
    val vaccineSerialNumber: String,
    val doctor: PersonnelDtoOut,
    val nurse: PersonnelDtoOut?,
    val notes: String?
)

fun VaccinationDetailDtoOut.toPatientVaccinationDetailDto() = PatientVaccinationDetailDto(
    vaccinationId = vaccinationId,
    patientId = patientId,
    bodyPart = bodyPart,
    vaccinatedOn = vaccinatedOn,
    vaccineSerialNumber = vaccineSerialNumber,
    userPerformingVaccination = doctor.id,
    nurseId = nurse?.id,
    notes = notes
)
