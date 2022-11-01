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
import dev.forst.katlib.validate
import dev.forst.katlib.whenFalse
import mu.KLogging
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import java.time.Instant

class VaccinationSlotService(
    private val locationRepository: LocationRepository,
    private val vaccinationSlotRepository: VaccinationSlotRepository,
) {
    private companion object : KLogging() {
        val DEFAULT_STATUS: VaccinationSlotStatus = VaccinationSlotStatus.ONLY_FREE
    }

    /**
     * Inserts given slots to the database.
     */
    suspend fun addSlots(createDto: CreateVaccinationSlotsDtoIn): List<EntityId> {
        validate(createDto.to >= createDto.from.plusMillis(createDto.durationMillis)) {
            throw InvalidSlotCreationRequest("Specified time range is not valid.", createDto)
        }
        return generateSlots(createDto)
    }

    private suspend fun generateSlots(slotsDto: CreateVaccinationSlotsDtoIn): List<EntityId> {
        locationRepository.locationIdExists(slotsDto.locationId)
            .whenFalse { throw entityNotFound<Locations>(Locations::id, slotsDto.locationId) }

        val availableTime = slotsDto.to.toEpochMilli() - slotsDto.from.toEpochMilli()
        val slotsCount = (availableTime / slotsDto.durationMillis).toInt()

        val slots = (0 until slotsCount).flatMap { slotId ->
            (0 until slotsDto.bandwidth).map { queue ->
                val queueId = queue + slotsDto.queueOffset
                val from = slotsDto.from.plusMillis(slotId * slotsDto.durationMillis)
                val to = from.plusMillis(slotsDto.durationMillis)

                VaccinationSlotDto(
                    locationId = slotsDto.locationId,
                    queue = queueId,
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
        status: VaccinationSlotStatus? = null
    ): List<VaccinationSlotDtoOut> {
        val fromI = from ?: defaultPostgresFrom
        val toI = to ?: defaultPostgresTo

        val usedStatus = status ?: if (slotId == null) DEFAULT_STATUS else VaccinationSlotStatus.ALL

        return vaccinationSlotRepository.getAndMap {
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
     * Book a vaccination slot for the patient.
     */
    suspend fun bookSlotForPatient(patientId: EntityId) =
        vaccinationSlotRepository.tryToBookSlotForPatient(patientId)
            ?: throw NoVaccinationSlotsFoundException()
}
