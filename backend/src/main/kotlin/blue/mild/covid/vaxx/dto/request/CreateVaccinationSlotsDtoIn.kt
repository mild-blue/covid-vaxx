package blue.mild.covid.vaxx.dto.request

import blue.mild.covid.vaxx.dao.model.EntityId
import java.time.Instant

data class CreateVaccinationSlotsDtoIn(
    val locationId: EntityId? = null,
    val from: Instant,
    val to: Instant,
    val durationSec: Int
)
