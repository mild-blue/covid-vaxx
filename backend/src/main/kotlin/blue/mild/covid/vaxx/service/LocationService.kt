package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Locations
import blue.mild.covid.vaxx.dao.repository.LocationRepository
import blue.mild.covid.vaxx.dto.request.LocationDtoIn
import blue.mild.covid.vaxx.dto.response.LocationDtoOut
import blue.mild.covid.vaxx.error.entityNotFound
import blue.mild.covid.vaxx.utils.formatPhoneNumber
import mu.KLogging
import java.util.*

class LocationService(
    private val locationRepository: LocationRepository,
//    private val vaccinationSlotRepository: VaccinationSlotRepository,
//    private val patientRepository: PatientRepository
) {

    private companion object : KLogging()

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

//    private fun <T> Op<Boolean>.andWithIfNotEmpty(value: T?, column: Column<T>): Op<Boolean> =
//        value?.let { and { column eq value } } ?: this
}
