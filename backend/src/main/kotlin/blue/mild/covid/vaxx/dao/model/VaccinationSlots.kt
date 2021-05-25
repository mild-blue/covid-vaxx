package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.`java-time`.timestamp

/**
 * Available time slot for vaccination on the [Locations].
 *
 * Attribute patientId is null if that slot is available.
 */
object VaccinationSlots : ManagedTable("vaccination_slots") {

    val locationId = locationReference()

    /**
     * Who was vaccinated.
     */
    val patientId = patientReference().nullable()

    /**
     * Identifier of the queue for locations with greater capacity
     */
    val queue = integer("queue")

    /**
     * When time slot starts.
     */
    val from = timestamp("from")

    /**
     * When time slot ends.
     */
    val to = timestamp("to")
}
