package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Locations
import blue.mild.covid.vaxx.dao.model.VaccinationSlots
import blue.mild.covid.vaxx.dao.repository.LocationRepository
import blue.mild.covid.vaxx.dao.repository.VaccinationSlotRepository
import blue.mild.covid.vaxx.dto.request.CreateVaccinationSlotsDtoIn
import blue.mild.covid.vaxx.dto.request.LocationDtoIn
import blue.mild.covid.vaxx.dto.response.LocationDtoOut
import blue.mild.covid.vaxx.dto.response.VaccinationSlotDtoOut
import blue.mild.covid.vaxx.error.entityNotFound
import blue.mild.covid.vaxx.utils.formatPhoneNumber
import mu.KLogging
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import java.time.Instant
import java.util.*

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
        val usedLocationId = (createDto.locationId ?: locationId)!!

        val location = locationRepository.getAndMapLocationsBy { Locations.id eq usedLocationId }
            .singleOrNull()
            ?.withSortedSlots() ?: throw entityNotFound<Locations>(Locations::id, usedLocationId)



        var ts = createDto.from
        val createdIds = mutableListOf<EntityId>()
        while (ts.plusMillis(createDto.durationMillis).isBefore(createDto.to.plusMillis(1))) {
            val to = ts.plusMillis(createDto.durationMillis)
            createdIds.add(
                vaccinationSlotRepository.addVaccinationSlot(
                    locationId=location.id,
                    patientId = null,
                    from = ts,
                    to = to,
                )
            )
            ts = to
        }

        return createdIds
    }

    /**
     * Filters the database with the conjunction (and clause) of the given properties.
     */
    suspend fun getSlotsByConjunctionOf(
        locationId: EntityId? = null,
        from: Instant,
        to: Instant,
        onlyFree: Boolean,
    ): List<VaccinationSlotDtoOut> =
        vaccinationSlotRepository.get {
            Op.TRUE
                .andWithIfNotEmpty(locationId, VaccinationSlots.locationId)
                .and {VaccinationSlots.from.greaterEq(from) }
                .and {VaccinationSlots.to.lessEq(to)}
                .and {if (onlyFree) VaccinationSlots.patientId.isNull() else VaccinationSlots.patientId.isNotNull() }
        }

    /**
     * Returns patient with given ID.
     */
    suspend fun getLocationById(locationId: EntityId): LocationDtoOut =
        locationRepository.getAndMapLocationsBy { Locations.id eq locationId }
            .singleOrNull()
            ?.withSortedSlots() ?: throw entityNotFound<Locations>(Locations::id, locationId)

//    /**
//     * Updates patient with given change set.
//     */
//    suspend fun updatePatientWithChangeSet(patientId: UUID, changeSet: PatientUpdateDtoIn) {
//        validationService.requireValidPatientUpdate(changeSet)
//
//        patientRepository.updatePatientChangeSet(
//            id = patientId,
//            firstName = changeSet.firstName?.trim(),
//            lastName = changeSet.lastName?.trim(),
//            zipCode = changeSet.zipCode,
//            district = changeSet.district?.trim(),
//            phoneNumber = changeSet.phoneNumber?.formatPhoneNumber(),
//            personalNumber = changeSet.personalNumber?.let { normalizePersonalNumber(it) },
//            email = changeSet.email?.trim()?.lowercase(Locale.getDefault()),
//            insuranceCompany = changeSet.insuranceCompany,
//            indication = changeSet.indication?.trim(),
//            answers = changeSet.answers?.associate { it.questionId to it.value }
//        ).whenFalse { throw entityNotFound<Patients>(Patients::id, patientId) }
//    }
//
    /**
     * Saves patient to the database and return its id.
     */
    suspend fun addLocation(location: LocationDtoIn): EntityId {
        logger.debug { "Adding location ${location.address}, ${location.zipCode}." }

        logger.debug { "Saving location." }
        return locationRepository.saveLocation(
            address = location.address.trim(),
            zipCode = location.zipCode,
            district = location.district.trim(),
            phoneNumber = location.phoneNumber?.formatPhoneNumber(),
            email = location.email?.trim()?.lowercase(Locale.getDefault()),
            notes = location.notes?.trim()
        ).also { locationId ->
            logger.debug { "Location ${location.address} saved under id $locationId." }
        }
    }
//
//    /**
//     * Deletes patient with given ID. Throws exception if patient was not deleted.
//     * */
//    suspend fun deletePatientById(patientId: UUID) {
//        val deletedCount = patientRepository.deletePatientsBy { Patients.id eq patientId }
//        if (deletedCount != 1) {
//            throw entityNotFound<Patients>(Patients::id, patientId)
//        }
//    }

//    private fun List<LocationDtoOut>.sorted() = map { it.withSortedSlots() }

    private fun LocationDtoOut.withSortedSlots() = copy(slots = slots.sortedBy { it.from })

    private fun <T> Op<Boolean>.andWithIfNotEmpty(value: T?, column: Column<T>): Op<Boolean> =
        value?.let { and { column eq value } } ?: this
}
