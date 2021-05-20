package blue.mild.covid.vaxx.dto.internal

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.VaccinationBodyPart
import java.time.Instant

data class StoreVaccinationRequestDto(
    val vaccinationId: EntityId,
    val patientId: EntityId,
    val bodyPart: VaccinationBodyPart,
    val vaccinatedOn: Instant,
    val vaccineSerialNumber: String,
    val userPerformingVaccination: EntityId,
    val nurseId: EntityId?,
    val notes: String?
)
