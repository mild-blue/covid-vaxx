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
    val durationMillis: Milliseconds
)
