package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.VaccinationSlots
import blue.mild.covid.vaxx.dao.model.Vaccinations
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
        from: Instant,
        to: Instant,
    ): EntityId = newSuspendedTransaction {
        VaccinationSlots.insert {
            it[VaccinationSlots.patientId] = patientId
            it[VaccinationSlots.locationId] = locationId
            it[VaccinationSlots.from] = from
            it[VaccinationSlots.to] = to
        }[VaccinationSlots.id]
    }

    /**
     * Updates vaccination slot entity with id [vaccinationSlotId].
     */
    suspend fun updateVaccinationSlot(
        vaccinationSlotId: EntityId,
        locationId: EntityId? = null,
        patientId: EntityId? = null,
        from: Instant? = null,
        to: Instant? = null,
    ): Boolean = newSuspendedTransaction {
        val isUpdateNecessary =
            locationId ?: patientId
            ?: from ?: to

        // if so, perform update query
        val vaccinationUpdated = if (isUpdateNecessary != null) {
            Vaccinations.update(
                where = { VaccinationSlots.id eq vaccinationSlotId },
                body = { row ->
                    row.apply {
                        updateIfNotNull(locationId, VaccinationSlots.locationId)
                        updateIfNotNull(patientId, VaccinationSlots.patientId)
                        updateIfNotNull(from, VaccinationSlots.from)
                        updateIfNotNull(to, VaccinationSlots.to)
                    }
                }
            )
        } else 0
        vaccinationUpdated == 1
    }

    suspend fun get(where: SqlExpressionBuilder.() -> Op<Boolean>) =
        newSuspendedTransaction {
            VaccinationSlots
                .select(where)
                ?.let {
                    data -> data.map {
                        VaccinationSlotDtoOut(
                            id = it[VaccinationSlots.id],
                            locationId = it[VaccinationSlots.locationId],
                            patientId = it[VaccinationSlots.patientId],
                            from = it[VaccinationSlots.from],
                            to = it[VaccinationSlots.to]
                        )
                    }
                }
        }

}
