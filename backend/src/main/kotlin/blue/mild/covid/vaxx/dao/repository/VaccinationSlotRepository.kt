package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.VaccinationSlots
import blue.mild.covid.vaxx.dto.internal.VaccinationSlotDto
import blue.mild.covid.vaxx.dto.response.VaccinationSlotDtoOut
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KLogging
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import pw.forst.katlib.TimeProvider
import java.time.Instant

class VaccinationSlotRepository(private val instantTimeProvider: TimeProvider<Instant>) {

    private companion object : KLogging() {
        val slotsBookingMutex = Mutex()
    }

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
    suspend fun getAndMap(where: SqlExpressionBuilder.() -> Op<Boolean>) =
        newSuspendedTransaction {
            VaccinationSlots
                .select(where)
                .orderBy(VaccinationSlots.from)
                .orderBy(VaccinationSlots.queue)
                .orderBy(VaccinationSlots.id)
                .toList()
                .let { data -> data.map { it.mapVaccinationSlot() } }
        }

    /**
     * Tries to book slot for patient [attemptsLeft] times. If it's not successful returns null.
     *
     * The attempts are here for distributed environment where some different pod might have booked the slot before
     * the one selecting the slot for booking. Thus we try multiple times and "hope for the best".
     *
     * TODO #267 this is reeeeeaaaaallly poor man's solution, but it works.. see concurrent booking test.
     * We need to achieve atomic update in the database, but unfortunately postgres does not support limit on update
     * and somehow lock the rows and then do not add them to filter why searching.
     */
    tailrec suspend fun tryToBookSlotForPatient(patientId: EntityId, attemptsLeft: Int): VaccinationSlotDtoOut? =
        if (attemptsLeft == 0) null
        else slotsBookingMutex.withLock {
            newSuspendedTransaction {
                VaccinationSlots
                    .select { VaccinationSlots.patientId.isNull() }
                    .orderBy(VaccinationSlots.from)
                    .orderBy(VaccinationSlots.queue)
                    .limit(1)
                    .singleOrNull() // fetch the first available slot
                    ?.getOrNull(VaccinationSlots.id)
                    ?.let { slotId ->
                        // book the slot for patient
                        VaccinationSlots.update(
                            // "and patientId is null" is here in order not to override already booked slots
                            where = { VaccinationSlots.id eq slotId and VaccinationSlots.patientId.isNull() },
                            body = { it[VaccinationSlots.patientId] = patientId },
                        )
                    }
                    ?.takeIf { it == 1 }
                    ?.also { commit() } // commit only if some slot was updated

                getAndMap { VaccinationSlots.patientId eq patientId }.singleOrNull()
            }
        } ?: tryToBookSlotForPatient(patientId, attemptsLeft - 1)


    private fun ResultRow.mapVaccinationSlot() = VaccinationSlotDtoOut(
        id = this[VaccinationSlots.id],
        locationId = this[VaccinationSlots.locationId],
        patientId = this[VaccinationSlots.patientId],
        queue = this[VaccinationSlots.queue],
        from = this[VaccinationSlots.from],
        to = this[VaccinationSlots.to]
    )
}
