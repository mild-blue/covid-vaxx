package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dao.repository.PatientRepository
import blue.mild.covid.vaxx.dto.request.CreateVaccinationSlotsDtoIn
import blue.mild.covid.vaxx.dto.request.LocationDtoIn
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
import org.kodein.di.DI
import org.kodein.di.instance
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


class VaccinationSlotRoutesTest : ServerTestBase() {

    private fun Instant.toUrlString() = mapper.writeValueAsString(this)
        .replace(":", "%3A")
        .replace("\"", "")

    private val location = LocationDtoIn(
        address = "AAAA",
        zipCode = 16000,
        district = "AAAA",
        phoneNumber = PhoneNumberDtoIn("+420724123456", "CZ"),
        email = "AAAA@test.com",
        notes = "AAAA - note"
    )
    private lateinit var locationId: EntityId
    private lateinit var locationIdOther: EntityId

    override fun populateDatabase(di: DI) {
        val locationService by di.instance<LocationService>()
        locationId = runBlocking { locationService.addLocation(location) }
        locationIdOther = runBlocking { locationService.addLocation(location.copy(address = "NIC MOC")) }
    }

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

    @Test
    fun `slots are created as expected`() = withTestApplication {
        val createSlots = CreateVaccinationSlotsDtoIn(
            locationId = locationId,
            from = Instant.ofEpochMilli(1000000),
            to = Instant.ofEpochMilli(3000000),
            durationMillis = 100000,
            bandwidth = 2,
        )
        // create slots
        handleRequest(HttpMethod.Post, Routes.vaccinationSlots) {
            authorize()
            jsonBody(createSlots)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<EntityId>>()
            assertEquals(40, slots.size)
        }

        // only authorized users can get slots
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter").run { expectStatus(HttpStatusCode.Unauthorized) }

        // get created slots
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(40, slots.size)
            slots.forEach { slot ->
                assertEquals(createSlots.locationId, slot.locationId)
                assertNull(slot.patientId)
                assertTrue { slot.from >= createSlots.from }
                assertTrue { slot.to <= createSlots.to }
                assertEquals(createSlots.durationMillis, slot.to.toEpochMilli() - slot.from.toEpochMilli())
            }
        }
    }

    @Test
    fun `can not create slots for non-existing location`() = withTestApplication {
        val nonExistingUuid = UUID.randomUUID()
        val createSlots = CreateVaccinationSlotsDtoIn(
            locationId = nonExistingUuid,
            from = Instant.ofEpochMilli(1000000),
            to = Instant.ofEpochMilli(3000000),
            durationMillis = 100000,
            bandwidth = 1,
        )

        handleRequest(HttpMethod.Post, Routes.vaccinationSlots) {
            authorize()
            jsonBody(createSlots)
        }.run {
            expectStatus(HttpStatusCode.NotFound)
        }
    }

    @Test
    fun `can not create slots if already exist`() = withTestApplication {
        val createSlots = CreateVaccinationSlotsDtoIn(
            locationId = locationId,
            from = Instant.ofEpochMilli(1000000),
            to = Instant.ofEpochMilli(3000000),
            durationMillis = 100000,
            bandwidth = 1,
        )

        handleRequest(HttpMethod.Post, Routes.vaccinationSlots) {
            authorize()
            jsonBody(createSlots)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<EntityId>>()
            assertEquals(20, slots.size)
        }

        handleRequest(HttpMethod.Post, Routes.vaccinationSlots) {
            authorize()
            jsonBody(createSlots)
        }.run {
            expectStatus(HttpStatusCode.Conflict)
        }

        // set offset for the queue this should allow new slots creation
        val validNewSlots = createSlots.copy(queueOffset = createSlots.bandwidth)
        handleRequest(HttpMethod.Post, Routes.vaccinationSlots) {
            authorize()
            jsonBody(validNewSlots)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<EntityId>>()
            assertEquals(20, slots.size)
        }

        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(40, slots.size)
        }
    }

    @Test
    @Suppress("LongMethod", "ComplexMethod") // its whole flow test, that's fine
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

        val slotsAll = runBlocking { vaccinationSlotService.getSlotsByConjunctionOf() }
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
        val from = Instant.ofEpochMilli(2000000).toUrlString()
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?from=$from") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(10 + 40, slots.size)
        }

        // restrict by to
        val to = Instant.ofEpochMilli(2000000).toUrlString()
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?to=$to") {
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
        val patientId = runBlocking {
            patientRepository.savePatient(
                "alice", "alice", 12345, "alice", "1", "1", null,"email", InsuranceCompany.CPZP, "indication", "remoteHost", mapOf(), null
            )
        }
        // reserve slot - it should be the first one
        runBlocking {
            vaccinationSlotService.bookSlotForPatient(patientId)
        }.let { slot ->
            assertEquals(slotsAll[0].id, slot.id)
            assertEquals(patientId, slot.patientId)
            assertEquals(0, slot.queue)
        }

        // get slots - default
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(59, slots.size)
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
            assertEquals(59, slots.size)
        }

        // get slots - OCCUPIED
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?status=${VaccinationSlotStatus.ONLY_OCCUPIED}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(1, slots.size)
        }

        // get slots - ALL with given patient
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?status=${VaccinationSlotStatus.ALL}&patientId=${patientId}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(1, slots.size)
        }
    }
}
