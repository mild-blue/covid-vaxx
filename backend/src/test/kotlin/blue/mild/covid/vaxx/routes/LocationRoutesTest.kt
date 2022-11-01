package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dto.request.LocationDtoIn
import blue.mild.covid.vaxx.dto.request.PhoneNumberDtoIn
import blue.mild.covid.vaxx.dto.response.EntityIdDtoOut
import blue.mild.covid.vaxx.dto.response.LocationDtoOut
import blue.mild.covid.vaxx.service.LocationService
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.instance
import kotlin.test.assertEquals

class LocationRoutesTest : ServerTestBase() {

    private val existingLocation = LocationDtoIn(
        address = "Bar Street 10",
        zipCode = 16006,
        district = "Dejvice",
        phoneNumber = PhoneNumberDtoIn("+420724123456", "CZ"),
        email = "location-1@mild.blue",
        notes = "location-2 - note"
    )

    lateinit var existingLocationId: EntityId

    // this code populates database before every test
    override fun populateDatabase(di: DI) {
        val locationService by di.instance<LocationService>()
        existingLocationId = runBlocking { locationService.addLocation(existingLocation) }
    }

    @Test
    fun `get existing location from the database`() = withTestApplication {
        handleRequest(HttpMethod.Get, Routes.publicLocations) {
            authorize()
        }.run {
            val locations = receive<List<LocationDtoOut>>()
            assertEquals(1, locations.size)

            val location = locations.first()
            assertEquals(existingLocationId, location.id)
            assertEquals(existingLocation.address, location.address)
        }
    }

    @Test
    fun `add location`() = withTestApplication {
        // try to login with non-existing user
        val location = LocationDtoIn(
            address = "Foo Street 1",
            zipCode = 16000,
            district = "Dejvice",
            phoneNumber = PhoneNumberDtoIn("+420724123456", "CZ"),
            email = "location-1@test.com",
            notes = "location-1 - note"
        )

        // verify that you need auth for that
        handleRequest(HttpMethod.Post, Routes.locations) {
            jsonBody(location)
        }.run {
            expectStatus(HttpStatusCode.Unauthorized)
        }

        val locationId = handleRequest(HttpMethod.Post, Routes.locations) {
            authorize()
            jsonBody(location)
        }.run {
            expectStatus(HttpStatusCode.OK)
            receive<EntityIdDtoOut>()
        }

        handleRequest(HttpMethod.Get, Routes.publicLocations + "/${locationId.id}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val response = receive<LocationDtoOut>()

            assertEquals(location.address, response.address)
            assertEquals(location.zipCode, response.zipCode)
            assertEquals(location.district, response.district)
            assertEquals(location.phoneNumber?.number, response.phoneNumber)
            assertEquals(location.email, response.email)
            assertEquals(location.notes, response.notes)
            assertEquals(locationId.id, response.id)
        }

        handleRequest(HttpMethod.Get, Routes.publicLocations) {
            authorize()
        }.run {
            val locations = receive<List<LocationDtoOut>>()
            assertEquals(2, locations.size)
        }
    }
}
