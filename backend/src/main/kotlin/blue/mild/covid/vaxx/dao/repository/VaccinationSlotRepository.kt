package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.VaccinationSlots
import blue.mild.covid.vaxx.dto.response.VaccinationSlotDtoOut
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

@Suppress("LongParameterList") // it's a repository, we're fine with this
class VaccinationSlotRepository {
    /**
     * Creates new vaccination slot record for given data.
     */
    suspend fun addVaccinationSlot(
        locationId: EntityId,
        patientId: EntityId? = null,
        queue: Int,
        from: Instant,
        to: Instant,
    ): EntityId = newSuspendedTransaction {
        VaccinationSlots.insert {
            it[VaccinationSlots.patientId] = patientId
            it[VaccinationSlots.locationId] = locationId
            it[VaccinationSlots.queue] = queue
            it[VaccinationSlots.from] = from
            it[VaccinationSlots.to] = to
        }[VaccinationSlots.id]
    }

    /**
     * Updates vaccination slot entity with id [vaccinationSlotId].
     */
    suspend fun updateVaccinationSlot(
        vaccinationSlotId: EntityId,
        patientId: EntityId? = null,
    ): Int = newSuspendedTransaction {
        VaccinationSlots.update(
            where = { VaccinationSlots.id eq vaccinationSlotId },
            body = { row ->
                row.apply {
                    this[VaccinationSlots.patientId] = patientId
                }
            }
        )
    }

    suspend fun get(where: SqlExpressionBuilder.() -> Op<Boolean>) =
        newSuspendedTransaction {
            VaccinationSlots
                .select(where)
                .orderBy(VaccinationSlots.from)
                .orderBy(VaccinationSlots.queue)
                .orderBy(VaccinationSlots.id)
                ?.let { data ->
                    data.map {
                        VaccinationSlotDtoOut(
                            id = it[VaccinationSlots.id],
                            locationId = it[VaccinationSlots.locationId],
                            patientId = it[VaccinationSlots.patientId],
                            queue = it[VaccinationSlots.queue],
                            from = it[VaccinationSlots.from],
                            to = it[VaccinationSlots.to]
                        )
                    }
                }
        }

}
