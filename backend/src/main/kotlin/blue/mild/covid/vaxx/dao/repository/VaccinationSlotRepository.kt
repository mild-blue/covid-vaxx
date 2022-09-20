package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.VaccinationSlots
import blue.mild.covid.vaxx.dto.internal.VaccinationSlotDto
import blue.mild.covid.vaxx.dto.response.VaccinationSlotDtoOut
import dev.forst.katlib.TimeProvider
import mu.KLogging
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

class VaccinationSlotRepository(private val instantTimeProvider: TimeProvider<Instant>) {

    private companion object : KLogging()

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
    suspend fun getAndMap(where: SqlExpressionBuilder.() -> Op<Boolean>) = getAndMap(where) { this }

    /**
     * Retrieves all vaccination slots from the database with given filter.
     */
    private suspend fun getAndMap(where: SqlExpressionBuilder.() -> Op<Boolean>, queryMutation: Query.() -> Query) =
        newSuspendedTransaction {
            VaccinationSlots
                .select(where)
                .orderBy(VaccinationSlots.from)
                .orderBy(VaccinationSlots.queue)
                .orderBy(VaccinationSlots.id)
                .queryMutation()
                .toList()
                .let { data -> data.map { it.mapVaccinationSlot() } }
        }

    /**
     * Return first available slot.
     */
    suspend fun getFirstAvailableSlot(): VaccinationSlotDtoOut? =
        getAndMap(where = { VaccinationSlots.patientId.isNull() }, queryMutation = { limit(1) })
            .firstOrNull()

    /**
     * Tries to book slot for patient [patientId], returns null if no slot was booked.
     */
    suspend fun tryToBookSlotForPatient(patientId: EntityId): VaccinationSlotDtoOut? {
        val patientIdName = VaccinationSlots.patientId.nameInDatabaseCase()
        val tableName = VaccinationSlots.nameInDatabaseCase()
        val idName = VaccinationSlots.id.nameInDatabaseCase()
        val fromName = VaccinationSlots.from.nameInDatabaseCase()
        val queueName = VaccinationSlots.queue.nameInDatabaseCase()

        // raw query that allows us to add "for update skip locked"
        // based on the first example in https://spin.atomicobject.com/2021/02/04/redis-postgresql/
        return newSuspendedTransaction {
            """
            update "$tableName"
            set "$patientIdName" = ?
            where "$idName" = (
                select s."$idName" from "$tableName" s
                where s."$patientIdName" is null
                order by "$fromName", "$queueName"
                limit 1 
                for update 
                skip locked
            )
            and $patientIdName is null
            """.trimIndent()
                .exec(listOf(VaccinationSlots.patientId to patientId))
        }.let { getAndMap { VaccinationSlots.patientId eq patientId }.singleOrNull() }
    }

    private fun ResultRow.mapVaccinationSlot() = VaccinationSlotDtoOut(
        id = this[VaccinationSlots.id],
        locationId = this[VaccinationSlots.locationId],
        patientId = this[VaccinationSlots.patientId],
        queue = this[VaccinationSlots.queue],
        from = this[VaccinationSlots.from],
        to = this[VaccinationSlots.to]
    )

    // execute native query
    private fun String.exec(params: Iterable<Pair<Column<*>, Any?>> = emptyList()) {
        TransactionManager.current()
            .exec(this, params.map { (column, value) -> column.columnType to value })
    }
}
