package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dto.request.LocationDtoIn
import blue.mild.covid.vaxx.dto.request.PhoneNumberDtoIn
import blue.mild.covid.vaxx.dto.response.LocationDtoOut
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class LocationRoutesTest : ServerTestBase() {
    @Test
    fun `add location`() = withTestApplication {
        // try to login with non-existing user
        val location = LocationDtoIn(
            address = "Foo Street 1",
            zipCode = 16000,
            district = "Dejvice",
            phoneNumber = PhoneNumberDtoIn("724123456", "CZ"),
            email = "location-1@test.com",
            note = "location-1 - note"
        )
        handleRequest(HttpMethod.Post, Routes.locations) {
            authorize()
            jsonBody(location)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val response = receive<EntityId>()

            handleRequest(HttpMethod.Post, Routes.locations + "/${response}") {
                authorize()
            }.run {
                expectStatus(HttpStatusCode.OK)
                val response = receive<LocationDtoOut>()
                assertEquals(location.address, response.address)
                assertEquals(location.zipCode, response.zipCode)
                assertEquals(location.district, response.district)
                assertEquals(location.phoneNumber?.toString(), response.phoneNumber)
                assertEquals(location.email, response.email)
                assertEquals(location.note, response.note)
                assertNotNull(response.id)
            }


        }
    }
}
