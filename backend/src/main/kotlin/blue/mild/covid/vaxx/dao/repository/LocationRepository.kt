package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Locations
import blue.mild.covid.vaxx.dao.model.Patients
import blue.mild.covid.vaxx.dao.model.VaccinationSlots
import blue.mild.covid.vaxx.dto.response.LocationDtoOut
import blue.mild.covid.vaxx.dto.response.VaccinationSlotDtoOut
import blue.mild.covid.vaxx.utils.createLogger
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

@Suppress("LongParameterList") // it's a repository, we're fine with this
class LocationRepository {
    val logger = createLogger("LocationRepository")

    /**
     * Updates location entity with given id. Change set of the given properties
     * is applied to the entity.
     *
     * Note: use named parameters while using this method.
     */
    suspend fun updateLocationChangeSet(
        id: EntityId,
        address: String? = null,
        zipCode: Int? = null,
        district: String? = null,
        phoneNumber: String? = null,
        email: String? = null,
        notes: String? = null,
    ): Boolean = newSuspendedTransaction {
        // check if any property is not null
        val isLocationEntityUpdateNecessary =
            address
            ?: district ?: zipCode
            ?: phoneNumber ?: email
            ?: notes
        // if so, perform update query
        val locationUpdated = if (isLocationEntityUpdateNecessary != null) {
            Locations.update(
                where = { Locations.id eq id },
                body = { row ->
                    row.apply {
                        updateIfNotNull(address, Locations.address)
                        updateIfNotNull(zipCode, Locations.zipCode)
                        updateIfNotNull(district, Locations.district)
                        updateIfNotNull(phoneNumber, Locations.phoneNumber)
                        updateIfNotNull(email, Patients.email)
                        updateIfNotNull(notes, Locations.notes)
                    }
                }
            )
        } else 0
        // indicate that patient was updated if patient entity was updated OR at least one answer was updated
        locationUpdated > 0
    }

    /**
     * Gets and maps locations by given where clause.
     *
     * If no clause is given, it returns and maps whole database.
     */
    suspend fun getAndMapLocationsBy(
        where: (SqlExpressionBuilder.() -> Op<Boolean>)? = null
    ): List<LocationDtoOut> = newSuspendedTransaction { getAndMapLocations(where) }

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

    /**
     * Deletes patient entity by given [where]. Returns number of deleted entities.
     */
    suspend fun deleteLocationsBy(where: (SqlExpressionBuilder.() -> Op<Boolean>)): Int =
        newSuspendedTransaction { Locations.deleteWhere(op = where) }


    private fun getAndMapLocations(where: (SqlExpressionBuilder.() -> Op<Boolean>)? = null) =
        Locations
            .leftJoin(VaccinationSlots, { id }, { locationId })
            .let { if (where != null) it.select(where) else it.selectAll() }
            .toList() // eager fetch all data from the database
            .let { data ->
                logger.info(data.toString())
                // MartinLlama - this is outer join, so slots are there only for some locations
                // I want to have slots equal to empty list or null if there are no slots
                // I have to hack it by using containsKey
                val slots = data.filter { it[VaccinationSlots.id] != null }.groupBy({ it[Locations.id] }, { it.mapSlots() })
                data.distinctBy { it[Locations.id] }
                    .map { mapLocation(it, if (slots.containsKey(it[Locations.id])) slots.getValue(it[Locations.id]) else ArrayList()) }
            }

    private fun mapLocation(row: ResultRow, slots: List<VaccinationSlotDtoOut>) = LocationDtoOut(
        id = row[Locations.id],
        address = row[Locations.address],
        zipCode = row[Locations.zipCode],
        district = row[Locations.district],
        phoneNumber = row[Locations.phoneNumber],
        email = row[Locations.email],
        notes = row[Locations.notes],
        slots = slots
    )

    private fun ResultRow.mapSlots() = VaccinationSlotDtoOut(
        id = this[VaccinationSlots.id],
        locationId = this[VaccinationSlots.locationId],
        patientId = this[VaccinationSlots.patientId],
        queue = this[VaccinationSlots.queue],
        from = this[VaccinationSlots.from],
        to = this[VaccinationSlots.to]
    )
}
