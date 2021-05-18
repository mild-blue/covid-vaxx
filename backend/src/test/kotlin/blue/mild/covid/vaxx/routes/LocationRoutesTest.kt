package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dto.request.CreateVaccinationSlotsDtoIn
import blue.mild.covid.vaxx.dto.request.LocationDtoIn
import blue.mild.covid.vaxx.dto.request.PhoneNumberDtoIn
import blue.mild.covid.vaxx.dto.response.LocationDtoOut
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import org.junit.jupiter.api.Test
import java.time.Instant
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
        handleRequest(HttpMethod.Post, Routes.locations) {
            authorize()
            jsonBody(location)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val locationId = receive<EntityId>()

            handleRequest(HttpMethod.Get, Routes.locations + "/${locationId}") {
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
                assertEquals(locationId, response.id)
            }

            val createSlots = CreateVaccinationSlotsDtoIn(
                from = Instant.ofEpochSecond(1000),
                to = Instant.ofEpochSecond(2000),
                durationSec = 100
            )

            handleRequest(HttpMethod.Post, Routes.locations + "/${locationId}/slots") {
                authorize()
                jsonBody(createSlots)
            }.run {
                expectStatus(HttpStatusCode.OK)
            }
        }
    }
}
