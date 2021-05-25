package blue.mild.covid.vaxx.dto.request

import blue.mild.covid.vaxx.dao.model.EntityId
import java.time.Instant

data class CreateVaccinationSlotsDtoIn(
    /**
     * Location id
     */
    val locationId: EntityId? = null,
    /**
     * When the location opens.
     */
    val from: Instant,
    /**
     * When the location closes.
     */
    val to: Instant,
    /**
     * How many people can be vaccinated in parallel.
     *
     * Number of queues.
     */
    val bandwidth: Int,
    /**
     * How long it takes to vaccinate single person.
     */
    val durationMillis: Long
)
