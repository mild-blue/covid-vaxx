package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.VaccinationBodyPart
import blue.mild.covid.vaxx.dto.internal.StoreVaccinationRequestDto
import java.time.Instant
import java.time.LocalDate

data class VaccinationDetailDtoOut(
    val vaccinationId: EntityId,
    val bodyPart: VaccinationBodyPart,
    val patientId: EntityId,
    val vaccinatedOn: Instant,
    val vaccineSerialNumber: String,
    val vaccineExpiration: LocalDate?,
    val doctor: PersonnelDtoOut,
    val nurse: PersonnelDtoOut?,
    val notes: String?,
    val exportedToIsinOn: Instant?
)

fun VaccinationDetailDtoOut.toPatientVaccinationDetailDto() = StoreVaccinationRequestDto(
    vaccinationId = vaccinationId,
    patientId = patientId,
    bodyPart = bodyPart,
    vaccinatedOn = vaccinatedOn,
    vaccineSerialNumber = vaccineSerialNumber,
    vaccineExpiration = vaccineExpiration,
    userPerformingVaccination = doctor.id,
    nurseId = nurse?.id,
    notes = notes
)
