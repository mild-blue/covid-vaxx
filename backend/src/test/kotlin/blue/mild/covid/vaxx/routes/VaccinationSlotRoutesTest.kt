package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dto.request.CreateVaccinationSlotsDtoIn
import blue.mild.covid.vaxx.dto.request.LocationDtoIn
import blue.mild.covid.vaxx.dto.request.PhoneNumberDtoIn
import blue.mild.covid.vaxx.dto.response.VaccinationSlotDtoOut
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
            durationMillis = 100
        )
        handleRequest(HttpMethod.Post, Routes.vaccinationSlots) {
            authorize()
            jsonBody(createSlots)
        }.run {
            expectStatus(HttpStatusCode.NotFound)
        }
    }

    @Suppress("LongMethod")
    @Test
    fun `foo bar`() = withTestApplication {
        val locationService by closestDI().instance<LocationService>()
        // val vaccinationSlotService by closestDI().instance<VaccinationSlotService>()

        // prepare location 1
        val location1 = LocationDtoIn(
            address = "AAAA",
            zipCode = 16000,
            district = "AAAA",
            phoneNumber = PhoneNumberDtoIn("+420724123456", "CZ"),
            email = "AAAA@test.com",
            notes = "AAAA - note"
        )
        val locationId1 = runBlocking { locationService.addLocation(location1) }

        // prepare slot
        val createSlots1 = CreateVaccinationSlotsDtoIn(
            locationId = locationId1,
            from = Instant.ofEpochMilli(1000000),
            to = Instant.ofEpochMilli(3000000),
            durationMillis = 100000,
        )

        handleRequest(HttpMethod.Post, Routes.vaccinationSlots) {
            authorize()
            jsonBody(createSlots1)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<EntityId>>()
            assertEquals(20, slots.size)
        }

        // prepare location 2
        val location2 = LocationDtoIn(
            address = "BBBB",
            zipCode = 19000,
            district = "BBBB",
            phoneNumber = PhoneNumberDtoIn("+420724123456", "CZ"),
            email = "BBBB@test.com",
            notes = "BBBB - note"
        )
        val locationId2 = runBlocking { locationService.addLocation(location2) }

        // prepare slot
        val createSlots2 = CreateVaccinationSlotsDtoIn(
            locationId = locationId2,
            from = Instant.ofEpochMilli(2000000),
            to = Instant.ofEpochMilli(5000000),
            durationMillis = 300000,
        )

        handleRequest(HttpMethod.Post, Routes.vaccinationSlots) {
            authorize()
            jsonBody(createSlots2)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<EntityId>>()
            assertEquals(10, slots.size)
        }


        // get all slots
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(30, slots.size)
        }

        // restrict by location
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?locationId=${locationId1}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(20, slots.size)
        }

        // restrict by from
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?fromMillis=2000000") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(20, slots.size)
        }

        // restrict by to
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?toMillis=2000000") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(10, slots.size)
        }

    }
}
