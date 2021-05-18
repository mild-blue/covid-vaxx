package blue.mild.covid.vaxx.dto.request

import java.time.Instant

data class CreateVaccinationSlotsDtoIn(
    val from: Instant,
    val to: Instant,
    val durationSec: Int
)
