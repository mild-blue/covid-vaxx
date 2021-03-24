package blue.mild.covid.vaxx.dto.request

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.VaccinationBodyPart
import java.time.Instant

data class VaccinationDtoIn(
    val patientId: EntityId,
    val bodyPart: VaccinationBodyPart,
    val vaccinatedOn: Instant,
    val notes: String? = null
)
