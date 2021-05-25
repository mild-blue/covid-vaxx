package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.EntityId
import java.time.Instant

data class VaccinationSlotDtoOut(
    val id: EntityId,
    val locationId: EntityId,
    val patientId: EntityId?,
    val from: Instant,
    val to: Instant
)
