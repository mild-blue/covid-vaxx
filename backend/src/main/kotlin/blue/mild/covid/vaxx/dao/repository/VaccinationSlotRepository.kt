package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.VaccinationSlots
import blue.mild.covid.vaxx.dto.internal.VaccinationSlotDto
import blue.mild.covid.vaxx.dto.response.VaccinationSlotDtoOut
import blue.mild.covid.vaxx.utils.applyIfNotNull
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pw.forst.katlib.TimeProvider
import java.time.Instant

class VaccinationSlotRepository(private val instantTimeProvider: TimeProvider<Instant>) {

    /**
     * Insert all slots to the database.
     */
    suspend fun batchInsertVaccinationSlots(
        slots: List<VaccinationSlotDto>
    ): List<EntityId> = newSuspendedTransaction {
        val now = instantTimeProvider.now()
        VaccinationSlots.batchInsert(slots, shouldReturnGeneratedValues = true) {
            this[VaccinationSlots.patientId] = it.patientId
            this[VaccinationSlots.locationId] = it.locationId
            this[VaccinationSlots.queue] = it.queue
            this[VaccinationSlots.from] = it.from
            this[VaccinationSlots.to] = it.to

            // we want to specify these values because DB defaults don't support in batch inserts
            // however, the real value will be set by the database once inserted
            this[VaccinationSlots.created] = now
            this[VaccinationSlots.updated] = now
        }.map { it[VaccinationSlots.id] }
    }

    /**
     * Retrieves all vaccination slots from the database with given filter.
     */
    suspend fun getAndMap(limit: Int? = null, where: SqlExpressionBuilder.() -> Op<Boolean>) =
        newSuspendedTransaction {
            VaccinationSlots
                .select(where)
                .orderBy(VaccinationSlots.from)
                .orderBy(VaccinationSlots.queue)
                .orderBy(VaccinationSlots.id)
                .applyIfNotNull(limit) { limit(it) }
                .toList()
                .let { data -> data.map { it.mapVaccinationSlot() } }
        }

    private fun ResultRow.mapVaccinationSlot() = VaccinationSlotDtoOut(
        id = this[VaccinationSlots.id],
        locationId = this[VaccinationSlots.locationId],
        patientId = this[VaccinationSlots.patientId],
        queue = this[VaccinationSlots.queue],
        from = this[VaccinationSlots.from],
        to = this[VaccinationSlots.to]
    )
}
