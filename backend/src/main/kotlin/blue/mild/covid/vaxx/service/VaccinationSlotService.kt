package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Locations
import blue.mild.covid.vaxx.dao.model.VaccinationSlots
import blue.mild.covid.vaxx.dao.repository.LocationRepository
import blue.mild.covid.vaxx.dao.repository.VaccinationSlotRepository
import blue.mild.covid.vaxx.dto.internal.VaccinationSlotDto
import blue.mild.covid.vaxx.dto.request.CreateVaccinationSlotsDtoIn
import blue.mild.covid.vaxx.dto.request.query.VaccinationSlotStatus
import blue.mild.covid.vaxx.dto.response.VaccinationSlotDtoOut
import blue.mild.covid.vaxx.error.InvalidSlotCreationRequest
import blue.mild.covid.vaxx.error.NoVaccinationSlotsFoundException
import blue.mild.covid.vaxx.error.entityNotFound
import blue.mild.covid.vaxx.utils.defaultPostgresFrom
import blue.mild.covid.vaxx.utils.defaultPostgresTo
import mu.KLogging
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pw.forst.katlib.validate
import pw.forst.katlib.whenFalse
import java.sql.Connection.TRANSACTION_SERIALIZABLE
import java.time.Instant

val DEFAULT_STATUS: VaccinationSlotStatus = VaccinationSlotStatus.ONLY_FREE

class VaccinationSlotService(
    private val locationRepository: LocationRepository,
    private val vaccinationSlotRepository: VaccinationSlotRepository,
) {
    private companion object : KLogging()

    /**
     * Inserts given slots to the database.
     */
    suspend fun addSlots(createDto: CreateVaccinationSlotsDtoIn): List<EntityId> {
        validate(createDto.to >= createDto.from.plusMillis(createDto.durationMillis)) {
            throw InvalidSlotCreationRequest("Specified time range is not valid.", createDto)
        }

        locationRepository.locationIdExists(createDto.locationId)
            .whenFalse { throw entityNotFound<Locations>(Locations::id, createDto.locationId) }

        val availableTime = createDto.to.toEpochMilli() - createDto.from.toEpochMilli()
        val slotsCount = (availableTime / createDto.durationMillis).toInt()

        val slots = (0 until slotsCount).flatMap { slotId ->
            (0 until createDto.bandwidth).map { queue ->
                val from = createDto.from.plusMillis(slotId * createDto.durationMillis)
                val to = from.plusMillis(createDto.durationMillis)

                VaccinationSlotDto(
                    locationId = createDto.locationId,
                    queue = queue,
                    from = from,
                    to = to,
                )
            }
        }

        return vaccinationSlotRepository.batchInsertVaccinationSlots(slots)
    }

    /**
     * Filters the database with the conjunction (and clause) of the given properties.
     */
    @Suppress("LongParameterList")
    suspend fun getSlotsByConjunctionOf(
        slotId: EntityId? = null,
        locationId: EntityId? = null,
        patientId: EntityId? = null,
        from: Instant? = null,
        to: Instant? = null,
        status: VaccinationSlotStatus? = null,
        limit: Int? = null
    ): List<VaccinationSlotDtoOut> {
        val fromI = from ?: defaultPostgresFrom
        val toI = to ?: defaultPostgresTo

        val usedStatus = status ?: if (slotId == null) DEFAULT_STATUS else VaccinationSlotStatus.ALL

        val filter: SqlExpressionBuilder.() -> Op<Boolean> = {
            Op.TRUE
                .andWithIfNotEmpty(slotId, VaccinationSlots.id)
                .andWithIfNotEmpty(locationId, VaccinationSlots.locationId)
                .andWithIfNotEmpty(patientId, VaccinationSlots.patientId)
                .and { VaccinationSlots.from greaterEq fromI }
                .and { VaccinationSlots.to lessEq toI }
                .and {
                    when (usedStatus) {
                        VaccinationSlotStatus.ALL -> Op.TRUE
                        VaccinationSlotStatus.ONLY_FREE -> VaccinationSlots.patientId.isNull()
                        VaccinationSlotStatus.ONLY_OCCUPIED -> VaccinationSlots.patientId.isNotNull()
                    }
                }
        }

        return vaccinationSlotRepository.getAndMap(filter, limit)
    }

    /**
     * Book a vaccination slot for the patient with given requirements
     * using AND clause. If property is null, we ignore it.
     */
    suspend fun bookSlotForPatient(
        patientId: EntityId,
        slotId: EntityId? = null,
        locationId: EntityId? = null,
        from: Instant? = null,
        to: Instant? = null
    ): VaccinationSlotDtoOut = newSuspendedTransaction(transactionIsolation = TRANSACTION_SERIALIZABLE) {
        getSlotsByConjunctionOf(
            slotId = slotId,
            locationId = locationId,
            from = from,
            to = to,
            status = VaccinationSlotStatus.ONLY_FREE,
            // select a single slot
            limit = 1
        ).singleOrNull()?.apply {
            // book slot for the given patient
            vaccinationSlotRepository.updateVaccinationSlot(
                vaccinationSlotId = this.id,
                patientId = patientId,
            )
        }?.let { getSlotsByConjunctionOf(slotId = it.id).single() }
    } ?: throw NoVaccinationSlotsFoundException()


    private inline fun <reified T> Op<Boolean>.andWithIfNotEmpty(value: T?, column: Column<T>): Op<Boolean> =
        value?.let { and { column eq value } } ?: this
}
