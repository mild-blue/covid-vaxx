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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KLogging
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import pw.forst.katlib.validate
import pw.forst.katlib.whenFalse
import java.time.Instant


class VaccinationSlotService(
    private val locationRepository: LocationRepository,
    private val vaccinationSlotRepository: VaccinationSlotRepository,
) {
    private companion object : KLogging() {
        val DEFAULT_STATUS: VaccinationSlotStatus = VaccinationSlotStatus.ONLY_FREE
        val slotsBookingMutex = Mutex()
    }

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

        return vaccinationSlotRepository.getAndMap(limit) {
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
    }

    private inline fun <reified T> Op<Boolean>.andWithIfNotEmpty(value: T?, column: Column<T>): Op<Boolean> =
        value?.let { and { column eq value } } ?: this

    /**
     * Book a vaccination slot for the patient. Tries five times to book a slot before failing to do so.
     */
    suspend fun bookSlotForPatient(patientId: EntityId) =
        tryToBookSlotForPatient(patientId, attemptsLeft = 5) ?: throw NoVaccinationSlotsFoundException()

    /**
     * Tries to book slot for patient [attemptsLeft] times. If it's not successful returns null.
     *
     * The attempts are here for distributed environment where some different pod might have booked the slot before
     * the one selecting the slot for booking. Thus we try multiple times and "hope for the best".
     *
     * TODO this is reeeeeaaaaallly poor man's solution, but it works.. see concurrent booking test.
     * We need to achieve atomic update in the database, but unfortunately postgres does not support limit on update
     * and somehow lock the rows and then do not add them to filter why searching.
     */
    private tailrec suspend fun tryToBookSlotForPatient(patientId: EntityId, attemptsLeft: Int): VaccinationSlotDtoOut? =
        if (attemptsLeft == 0) null
        else slotsBookingMutex.withLock {
            newSuspendedTransaction {
                VaccinationSlots
                    .select { VaccinationSlots.patientId.isNull() }
                    .orderBy(VaccinationSlots.from)
                    .orderBy(VaccinationSlots.queue)
                    .limit(1)
                    .singleOrNull()
                    ?.getOrNull(VaccinationSlots.id)
                    ?.let { slotId ->
                        VaccinationSlots.update(
                            where = { VaccinationSlots.id eq slotId and VaccinationSlots.patientId.isNull() },
                            body = { it[VaccinationSlots.patientId] = patientId },
                        )
                    }

                commit()

                vaccinationSlotRepository
                    .getAndMap { VaccinationSlots.patientId eq patientId }
                    .singleOrNull()
            }
        } ?: tryToBookSlotForPatient(patientId, attemptsLeft - 1)
}
