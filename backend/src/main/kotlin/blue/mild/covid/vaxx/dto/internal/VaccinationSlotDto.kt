package blue.mild.covid.vaxx.dto.internal

import blue.mild.covid.vaxx.dao.model.EntityId
import java.time.Instant

data class VaccinationSlotDto(
    val locationId: EntityId,
    val queue: Int,
    val from: Instant,
    val to: Instant,
    val patientId: EntityId? = null
)
