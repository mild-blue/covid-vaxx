package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dao.repository.PatientRepository
import blue.mild.covid.vaxx.dto.request.CreateVaccinationSlotsDtoIn
import blue.mild.covid.vaxx.dto.request.LocationDtoIn
import blue.mild.covid.vaxx.dto.request.PatientVaccinationSlotSelectionDtoIn
import blue.mild.covid.vaxx.dto.request.PhoneNumberDtoIn
import blue.mild.covid.vaxx.dto.request.query.VaccinationSlotStatus
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
            durationMillis = 100,
            bandwidth = 10,
        )
        handleRequest(HttpMethod.Post, Routes.vaccinationSlots) {
            authorize()
            jsonBody(createSlots)
        }.run {
            expectStatus(HttpStatusCode.NotFound)
        }
    }

    @Suppress("LongMethod", "ComplexMethod")
    @Test
    fun `select slot basic workflow`() = withTestApplication {
        val locationService by closestDI().instance<LocationService>()
        val vaccinationSlotService by closestDI().instance<VaccinationSlotService>()
        val patientRepository by closestDI().instance<PatientRepository>()

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
            bandwidth = 1,
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
            bandwidth = 4,
        )

        handleRequest(HttpMethod.Post, Routes.vaccinationSlots) {
            authorize()
            jsonBody(createSlots2)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<EntityId>>()
            assertEquals(40, slots.size)
        }

        val slotsAll  = runBlocking { vaccinationSlotService.getSlotsByConjunctionOf() }
        assertEquals(60, slotsAll.size)

        // get all slots
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(60, slots.size)
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
            assertEquals(10 + 40, slots.size)
        }

        // restrict by to
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?toMillis=2000000") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(10, slots.size)
        }

        // restrict by id
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?id=${slotsAll[0].id}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(1, slots.size)
        }

        // create patient
        val patientId = runBlocking { patientRepository.savePatient(
            "alice", "alice", 12345, "alice", "1", "1", "email", InsuranceCompany.CPZP, "indication", "remoteHost", mapOf()
        ) }
        val patientDtoIn = PatientVaccinationSlotSelectionDtoIn(patientId)

        // reserve slot - it should be the first one
        handleRequest(HttpMethod.Post, "${Routes.vaccinationSlots}/filter") {
            authorize()
            jsonBody(patientDtoIn)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slot = receive<VaccinationSlotDtoOut>()
            assertEquals(slotsAll[0].id, slot.id)
            assertEquals(patientId, slot.patientId)
            assertEquals(0, slot.queue)
        }

        // first location has one less free slot
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?locationId=${locationId1}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(19, slots.size)
        }

        // second location has still the same amount of slots
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?locationId=${locationId2}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(40, slots.size)
        }

        // reserve slot by specifying location and minimal from
        handleRequest(HttpMethod.Post, "${Routes.vaccinationSlots}/filter?locationId=${locationId2}&fromMillis=3500000") {
            authorize()
            jsonBody(patientDtoIn)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slot = receive<VaccinationSlotDtoOut>()
            // it should be the middle one for the second location
            assertEquals(slotsAll[20 + 5 * 4].id, slot.id)
            assertEquals(patientId, slot.patientId)
            assertEquals(0, slot.queue)
        }

        // first location has the same amount of free slots as before
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?locationId=${locationId1}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(19, slots.size)
        }

        // second location has one free slot less
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?locationId=${locationId2}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(39, slots.size)
        }

        // get slots - default
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(58, slots.size)
        }

        // get slots - ALL
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?status=${VaccinationSlotStatus.ALL}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(60, slots.size)
        }

        // get slots - FREE
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?status=${VaccinationSlotStatus.ONLY_FREE}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(58, slots.size)
        }

        // get slots - OCCUPIED
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?status=${VaccinationSlotStatus.ONLY_OCCUPIED}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(2, slots.size)
        }

        // get occopied slots
        val slotsOccupied  = runBlocking { vaccinationSlotService.getSlotsByConjunctionOf(status = VaccinationSlotStatus.ONLY_OCCUPIED) }
        val firstOccupiedId = slotsOccupied[0].id

        // reserve slot by specifying location and minimal from
        handleRequest(HttpMethod.Post, "${Routes.vaccinationSlots}/filter?id=${firstOccupiedId}") {
            authorize()
            jsonBody(PatientVaccinationSlotSelectionDtoIn(null))
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slot = receive<VaccinationSlotDtoOut>()
            // it should be the first one
            assertEquals(slotsAll[0].id, slot.id)
            assertEquals(null, slot.patientId)
        }

        // get slots - FREE - there will be one more
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?status=${VaccinationSlotStatus.ONLY_FREE}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(59, slots.size)
        }

        // get slots - OCCUPIED - there will be one less
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?status=${VaccinationSlotStatus.ONLY_OCCUPIED}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(1, slots.size)
        }
    }
}
