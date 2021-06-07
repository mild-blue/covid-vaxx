package blue.mild.covid.vaxx.dto.request

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.utils.Milliseconds
import blue.mild.covid.vaxx.utils.PeopleVaccinatedInParallel
import java.time.Instant

data class CreateVaccinationSlotsDtoIn(
    /**
     * Location id.
     */
    val locationId: EntityId,
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
    val bandwidth: PeopleVaccinatedInParallel,
    /**
     * How long it takes to vaccinate single person.
     */
    val durationMillis: Milliseconds,

    /**
     * First used queue id - when there are already "n" queues in the database
     * for the given from/to time period and you want to use the same date interval,
     * you need to set [queueOffset] to "n" to create new slots.
     */
    val queueOffset: Int = 0
)
