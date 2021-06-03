package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dao.model.Questions
import blue.mild.covid.vaxx.dao.model.VaccinationSlots
import blue.mild.covid.vaxx.dao.repository.PatientRepository
import blue.mild.covid.vaxx.dto.request.AnswerDtoIn
import blue.mild.covid.vaxx.dto.request.ConfirmationDtoIn
import blue.mild.covid.vaxx.dto.request.CreateVaccinationSlotsDtoIn
import blue.mild.covid.vaxx.dto.request.LocationDtoIn
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.dto.request.PatientVaccinationSlotSelectionDtoIn
import blue.mild.covid.vaxx.dto.request.PhoneNumberDtoIn
import blue.mild.covid.vaxx.dto.request.query.VaccinationSlotStatus
import blue.mild.covid.vaxx.dto.response.VaccinationSlotDtoOut
import blue.mild.covid.vaxx.service.LocationService
import blue.mild.covid.vaxx.service.VaccinationSlotService
import blue.mild.covid.vaxx.utils.ServerTestBase
import blue.mild.covid.vaxx.utils.generatePersonalNumber
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.instance
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals


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

        handleRequest(HttpMethod.Post, Routes.vaccinationSlots) {
            authorize()
            jsonBody(createSlots)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<EntityId>>()
            assertEquals(40, slots.size)
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
    }

    @Test
    fun `test concurrent booking`() = withTestApplication {
        val from = 1000000L
        val to = 5000000L
        val createSlots = CreateVaccinationSlotsDtoIn(
            locationId = locationId,
            from = Instant.ofEpochMilli(from),
            to = Instant.ofEpochMilli(to),
            durationMillis = 100000L,
            bandwidth = 5,
        )

        val vaccinationSlotService by closestDI().instance<VaccinationSlotService>()
        val totalSlots = runBlocking { vaccinationSlotService.addSlots(createSlots) }.size

        val answers = transaction {
            Questions.selectAll().map { AnswerDtoIn(it[Questions.id], true) }
        }
        val generatePatient: () -> PatientRegistrationDtoIn = { generatePatientRegistrationDto(answers) }
        val bookedSlots = runBlocking {
            (0 until totalSlots).map {
                async {
                    handleRequest(HttpMethod.Post, "${Routes.patient}?captcha=disabled") {
                        jsonBody(generatePatient())
                    }.run {
                        expectStatus(HttpStatusCode.OK)
                        receive<VaccinationSlotDtoOut>()
                    }
                }
            }.awaitAll()
        }

        assertEquals(totalSlots, bookedSlots.size)
        // check that each slot was booked just once
        bookedSlots.groupBy({ it.id }, { it.patientId })
            .forEach { (_, patientId) ->
                assertEquals(1, patientId.count())
            }

        val totalSlotsInDatabase = transaction { VaccinationSlots.selectAll().count().toInt() }
        assertEquals(totalSlots, totalSlotsInDatabase)

        val bookedSlotsInDatabase = transaction { VaccinationSlots.select { VaccinationSlots.patientId.isNotNull() }.count().toInt() }
        assertEquals(totalSlots, bookedSlotsInDatabase)

        val freeSlots = transaction { VaccinationSlots.select { VaccinationSlots.patientId.isNull() }.count().toInt() }
        assertEquals(0, freeSlots)

        val patientsSlots = transaction {
            VaccinationSlots.selectAll()
                .groupBy { it[VaccinationSlots.patientId] }.mapValues { (_, rows) -> rows.count() }
        }
        assertEquals(totalSlots, patientsSlots.size)
        patientsSlots.forEach { (_, count) -> assertEquals(1, count) }
    }

    private fun generatePatientRegistrationDto(answers: List<AnswerDtoIn>) = PatientRegistrationDtoIn(
        firstName = UUID.randomUUID().toString(),
        lastName = UUID.randomUUID().toString(),
        zipCode = 1600,
        district = "Praha 6",
        personalNumber = generatePersonalNumber(),
        phoneNumber = PhoneNumberDtoIn("721680111", "CZ"),
        email = "${UUID.randomUUID()}@mild.blue",
        insuranceCompany = InsuranceCompany.values().random(),
        indication = null,
        answers = answers,
        confirmation = ConfirmationDtoIn(
            healthStateDisclosureConfirmation = true,
            covid19VaccinationAgreement = true,
            gdprAgreement = true
        )
    )

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
        var from = Instant.ofEpochMilli(2000000).toUrlString()
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
                "alice", "alice", 12345, "alice", "1", "1", "email", InsuranceCompany.CPZP, "indication", "remoteHost", mapOf(), false
            )
        }
        val patientDtoIn = PatientVaccinationSlotSelectionDtoIn(patientId)

        // reserve slot - it should be the first one
        handleRequest(HttpMethod.Post, "${Routes.vaccinationSlots}/book") {
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
        from = Instant.ofEpochMilli(3500000).toUrlString()
        handleRequest(HttpMethod.Post, "${Routes.vaccinationSlots}/book?locationId=${locationId2}&from=$from") {
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

        // get slots - ALL with given patient
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?status=${VaccinationSlotStatus.ALL}&patientId=${patientId}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(2, slots.size)
        }

        // get slots - FREE - there will be one more
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?status=${VaccinationSlotStatus.ONLY_FREE}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(58, slots.size)
        }

        // get slots - OCCUPIED - there will be one less
        handleRequest(HttpMethod.Get, "${Routes.vaccinationSlots}/filter?status=${VaccinationSlotStatus.ONLY_OCCUPIED}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val slots = receive<List<VaccinationSlotDtoOut>>()
            assertEquals(2, slots.size)
        }
    }
}
