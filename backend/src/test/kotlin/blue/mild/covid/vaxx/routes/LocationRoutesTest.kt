package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dto.request.LocationDtoIn
import blue.mild.covid.vaxx.dto.request.PhoneNumberDtoIn
import blue.mild.covid.vaxx.dto.response.EntityIdDtoOut
import blue.mild.covid.vaxx.dto.response.LocationDtoOut
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class LocationRoutesTest : ServerTestBase() {
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
            // MartinLlama: Figure out how to compare phone numbers
            assertEquals(location.phoneNumber?.number, response.phoneNumber)
            assertEquals(location.email, response.email)
            assertEquals(location.notes, response.notes)
            assertEquals(locationId.id, response.id)
        }
    }
}
