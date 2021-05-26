package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Locations
import blue.mild.covid.vaxx.dao.model.VaccinationSlots
import blue.mild.covid.vaxx.dao.repository.LocationRepository
import blue.mild.covid.vaxx.dao.repository.VaccinationSlotRepository
import blue.mild.covid.vaxx.dto.request.CreateVaccinationSlotsDtoIn
import blue.mild.covid.vaxx.dto.request.query.VaccinationSlotStatus
import blue.mild.covid.vaxx.dto.response.VaccinationSlotDtoOut
import blue.mild.covid.vaxx.error.entityNotFound
import mu.KLogging
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import java.time.Instant

@Suppress("MagicNumber")
val MAX_FUTURE: Instant = Instant.now().plusMillis(2L*365*24*60*60*1000)
val DEFAULT_STATUS: VaccinationSlotStatus = VaccinationSlotStatus.ONLY_FREE

class VaccinationSlotService(
    private val locationRepository: LocationRepository,
    private val vaccinationSlotRepository: VaccinationSlotRepository,
//    private val patientRepository: PatientRepository
) {
    private companion object : KLogging()

    @Suppress("TooGenericExceptionThrown", "ThrowsCount")
    suspend fun addSlots(createDto: CreateVaccinationSlotsDtoIn, locationId: EntityId? = null): List<EntityId> {
        if (createDto.to < createDto.from.plusMillis(createDto.durationMillis)) {
            throw Exception("Specified time range is not valid - ${createDto}")
        }

        if (createDto.locationId != null && locationId != null) {
            throw Exception("Location mismatch - ${createDto} vs ${locationId}")
        }
        val usedLocationId = requireNotNull(createDto.locationId ?: locationId) {
            "LocationId has to be specified - createDto: ${createDto}; locationId: ${locationId}."
        }

        val location = locationRepository.getAndMapLocationsBy { Locations.id eq usedLocationId }
            .singleOrNull() ?: throw entityNotFound<Locations>(Locations::id, usedLocationId)

        var ts = createDto.from
        val createdIds = mutableListOf<EntityId>()
        while (ts.plusMillis(createDto.durationMillis).isBefore(createDto.to.plusMillis(1))) {
            val to = ts.plusMillis(createDto.durationMillis)
            for (queue in 0 until createDto.bandwidth) {
                createdIds.add(
                    vaccinationSlotRepository.addVaccinationSlot(
                        locationId = location.id,
                        patientId = null,
                        queue = queue,
                        from = ts,
                        to = to,
                    )
                )
            }
            ts = to
        }

        return createdIds
    }

    /**
     * Filters the database with the conjunction (and clause) of the given properties.
     */
    @Suppress("LongParameterList")
    suspend fun getSlotsByConjunctionOf(
        id: EntityId? = null,
        locationId: EntityId? = null,
        patientId: EntityId? = null,
        fromMillis: Long? = null,
        toMillis: Long? = null,
        status: VaccinationSlotStatus? = DEFAULT_STATUS,
    ): List<VaccinationSlotDtoOut> {
        // To Consider: Maybe when id is specified all the other parameter should be
        val fromI = Instant.ofEpochMilli(fromMillis ?: Instant.EPOCH.toEpochMilli())
        // MartinLLama: With Instant.MAX it was failing on Exception occurred in the application: long overflow
        val toI = Instant.ofEpochMilli(toMillis ?: MAX_FUTURE.toEpochMilli())
        val usedStatus = status ?: if (id == null) DEFAULT_STATUS else VaccinationSlotStatus.ALL

        return vaccinationSlotRepository.get {
            Op.TRUE
                .andWithIfNotEmpty(id, VaccinationSlots.id)
                .andWithIfNotEmpty(locationId, VaccinationSlots.locationId)
                .andWithIfNotEmpty(patientId, VaccinationSlots.patientId)
                .and { VaccinationSlots.from.greaterEq(fromI) }
                .and { VaccinationSlots.to.lessEq(toI) }
                .and { when(usedStatus) {
                    // MartinLLama: I do not know how to write ALL to not perform any additional filtering
                    VaccinationSlotStatus.ALL -> {VaccinationSlots.id.isNotNull()}
                    VaccinationSlotStatus.ONLY_FREE -> VaccinationSlots.patientId.isNull()
                    VaccinationSlotStatus.ONLY_OCCUPIED -> VaccinationSlots.patientId.isNotNull()
                }}
        }
    }

    @Suppress("TooGenericExceptionThrown", "LongParameterList")
    suspend fun updateSlot(
        id: EntityId? = null,
        locationId: EntityId? = null,
        patientId: EntityId? = null,
        fromMillis: Long? = null,
        toMillis: Long? = null,
        status: VaccinationSlotStatus?,
        newPatientId: EntityId?,
    ): VaccinationSlotDtoOut {
        val availableSlots = getSlotsByConjunctionOf(
            id = id,
            locationId = locationId,
            patientId = patientId,
            fromMillis = fromMillis,
            toMillis = toMillis,
            status = status,
        )

        if (availableSlots.isEmpty()) {
            throw Exception("There are no available slots")
        }

        val pickedSlotId = availableSlots[0].id
        vaccinationSlotRepository.updateVaccinationSlot(
            vaccinationSlotId = pickedSlotId,
            patientId = newPatientId,
        )

        return vaccinationSlotRepository.get { VaccinationSlots.id.eq(pickedSlotId) }[0]
    }

    private fun <T> Op<Boolean>.andWithIfNotEmpty(value: T?, column: Column<T>): Op<Boolean> =
        value?.let { and { column eq value } } ?: this
}
