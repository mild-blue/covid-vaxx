package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Locations
import blue.mild.covid.vaxx.dao.repository.LocationRepository
import blue.mild.covid.vaxx.dto.request.LocationDtoIn
import blue.mild.covid.vaxx.dto.response.LocationDtoOut
import blue.mild.covid.vaxx.error.entityNotFound
import blue.mild.covid.vaxx.utils.formatPhoneNumber
import mu.KLogging
import java.util.Locale

class LocationService(
    private val locationRepository: LocationRepository
) {

    private companion object : KLogging()

    /**
     * Returns all locations.
     */
    suspend fun getAllLocations(): List<LocationDtoOut> =
        locationRepository.getAllLocationsWithoutSlots()

    /**
     * Returns patient with given ID.
     */
    suspend fun getLocationById(locationId: EntityId): LocationDtoOut =
        locationRepository.getLocation(locationId)
            ?: throw entityNotFound<Locations>(Locations::id, locationId)

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
}
