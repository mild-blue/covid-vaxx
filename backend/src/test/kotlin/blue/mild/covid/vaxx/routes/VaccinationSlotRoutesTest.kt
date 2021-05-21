package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dto.request.CreateVaccinationSlotsDtoIn
import blue.mild.covid.vaxx.dto.request.LocationDtoIn
import blue.mild.covid.vaxx.dto.request.PhoneNumberDtoIn
import blue.mild.covid.vaxx.service.LocationService
import blue.mild.covid.vaxx.service.VaccinationSlotService
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.kodein.di.instance
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals


class VaccinationSlotRoutesTest : ServerTestBase() {
    @Test
    fun `add slots for not-existing location should fail`() = withTestApplication {
        val createSlots = CreateVaccinationSlotsDtoIn(
            locationId = UUID.randomUUID(),
            from = Instant.ofEpochSecond(1000),
            to = Instant.ofEpochSecond(2000),
            durationSec = 100
        )
        handleRequest(HttpMethod.Post, Routes.vaccinationSlots) {
            authorize()
            jsonBody(createSlots)
        }.run {
            expectStatus(HttpStatusCode.NotFound)
        }
    }

    @Test
    fun `foo bar`() = withTestApplication {
        val locationService by closestDI().instance<LocationService>()
        val vaccinationSlotService by closestDI().instance<VaccinationSlotService>()

        // prepare location
        val location = LocationDtoIn(
            address = "Foo Street 1",
            zipCode = 16000,
            district = "Dejvice",
            phoneNumber = PhoneNumberDtoIn("+420724123456", "CZ"),
            email = "location-1@test.com",
            notes = "location-1 - note"
        )
        val locationId = runBlocking { locationService.addLocation(location) }

        // prepare slot
        val createSlots = CreateVaccinationSlotsDtoIn(
            locationId = locationId,
            from = Instant.ofEpochSecond(1000),
            to = Instant.ofEpochSecond(2000),
            durationSec = 100,
        )

        handleRequest(HttpMethod.Post, Routes.vaccinationSlots) {
            authorize()
            jsonBody(createSlots)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<EntityId>>()
            assertEquals(10, slots.size)
        }

    }
}
