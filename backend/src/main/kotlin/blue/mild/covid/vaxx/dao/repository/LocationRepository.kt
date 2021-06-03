package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Locations
import blue.mild.covid.vaxx.dto.response.LocationDtoOut
import mu.KLogging
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

@Suppress("LongParameterList") // it's a repository, we're fine with this
class LocationRepository {

    private companion object : KLogging()

    /**
     * Gets all locations without slots.
     */
    suspend fun getAllLocationsWithoutSlots(): List<LocationDtoOut> = newSuspendedTransaction {
        Locations.selectAll().map { it.mapLocation() }
    }

    /**
     * Returns location with all slots and their statuses.
     */
    suspend fun getLocation(id: EntityId): LocationDtoOut? = newSuspendedTransaction {
        Locations.select { Locations.id eq id }.singleOrNull()?.mapLocation()
    }

    suspend fun locationIdExists(id: EntityId) = newSuspendedTransaction {
        Locations.select { Locations.id eq id }.count() == 1L
    }

    /**
     * Saves the given data to the database as a new location record.
     *
     * Note: use named parameters while using this method.
     */
    // this can't be refactored as it is builder
    @Suppress("LongParameterList")
    suspend fun saveLocation(
        address: String,
        zipCode: Int,
        district: String,
        phoneNumber: String? = null,
        email: String? = null,
        notes: String? = null,
    ): EntityId = newSuspendedTransaction {
        Locations.insert {
            it[Locations.address] = address
            it[Locations.zipCode] = zipCode
            it[Locations.district] = district
            it[Locations.phoneNumber] = phoneNumber
            it[Locations.email] = email
            it[Locations.notes] = notes
        }[Locations.id]
    }

    private fun ResultRow.mapLocation() = LocationDtoOut(
        id = this[Locations.id],
        address = this[Locations.address],
        zipCode = this[Locations.zipCode],
        district = this[Locations.district],
        phoneNumber = this[Locations.phoneNumber],
        email = this[Locations.email],
        notes = this[Locations.notes]
    )
}
