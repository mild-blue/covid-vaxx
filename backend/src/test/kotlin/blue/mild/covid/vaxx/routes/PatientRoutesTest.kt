package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Patients
import blue.mild.covid.vaxx.dao.model.Questions
import blue.mild.covid.vaxx.dao.model.VaccinationSlots
import blue.mild.covid.vaxx.dto.internal.IsinValidationResultDto
import blue.mild.covid.vaxx.dto.internal.PatientValidationResult
import blue.mild.covid.vaxx.dto.request.AnswerDtoIn
import blue.mild.covid.vaxx.dto.request.ConfirmationDtoIn
import blue.mild.covid.vaxx.dto.request.CreateVaccinationSlotsDtoIn
import blue.mild.covid.vaxx.dto.request.LocationDtoIn
import blue.mild.covid.vaxx.dto.request.PatientUpdateDtoIn
import blue.mild.covid.vaxx.dto.request.PhoneNumberDtoIn
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import blue.mild.covid.vaxx.dto.response.PatientRegistrationResponseDtoOut
import blue.mild.covid.vaxx.generators.generatePatientRegistrationDto
import blue.mild.covid.vaxx.service.LocationService
import blue.mild.covid.vaxx.service.PatientValidationService
import blue.mild.covid.vaxx.service.VaccinationSlotService
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PatientRoutesTest : ServerTestBase() {

    private val location = LocationDtoIn(
        address = "AAAA",
        zipCode = 16000,
        district = "AAAA",
        phoneNumber = PhoneNumberDtoIn("+420724123456", "CZ"),
        email = "AAAA@test.com",
        notes = "AAAA - note"
    )
    private val patientRoute = "${Routes.patient}?captcha=nic"

    private lateinit var locationId: EntityId

    override fun populateDatabase(di: DI) {
        val locationService by di.instance<LocationService>()
        locationId = runBlocking { locationService.addLocation(location) }
    }

    override fun overrideDIContainer() = DI {
        bind<PatientValidationService>() with singleton {
            val service = mockk<PatientValidationService>()
            coEvery {
                service.validatePatient(any(), any(), any())
            } returns IsinValidationResultDto(PatientValidationResult.PATIENT_FOUND, "10")

            service
        }
    }

    @Test
    fun `foreigner and normal patient registration`() = withTestApplication {
        val createSlots = CreateVaccinationSlotsDtoIn(
            locationId = locationId,
            from = Instant.ofEpochMilli(20),
            to = Instant.ofEpochMilli(30),
            durationMillis = 10,
            bandwidth = 10,
        )
        val slotService by closestDI().instance<VaccinationSlotService>()
        val slots = runBlocking { slotService.addSlots(createSlots) }
        assertEquals(10, slots.size)

        // create patient
        val validRegistration = runBlocking { generatePatientRegistrationDto() }
        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(validRegistration)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val registration = receive<PatientRegistrationResponseDtoOut>()
            registration.patientId
        }

        // create another patient (so insuranceNumber is null two times)
        val anotherValidRegistration = runBlocking { generatePatientRegistrationDto() }
        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(anotherValidRegistration)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val registration = receive<PatientRegistrationResponseDtoOut>()
            registration.patientId
        }

        // create foreigner
        val validForeigner = validRegistration.copy(personalNumber = null, insuranceNumber = "something")
        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(validForeigner)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val registration = receive<PatientRegistrationResponseDtoOut>()
            registration.patientId
        }

        // create another foreigner
        val validForeigner2 = validRegistration.copy(personalNumber = null, insuranceNumber = "something2")
        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(validForeigner2)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val registration = receive<PatientRegistrationResponseDtoOut>()
            registration.patientId
        }

        // verify that it is not possible to register patient without insuranceNumber AND personalNumber
        val invalidForeigner = validForeigner.copy(personalNumber = null, insuranceNumber = null)
        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(invalidForeigner)
        }.run {
            expectStatus(HttpStatusCode.BadRequest)
        }

        // verify that it is not possible to register two foreigners with the same insuranceNumber
        val duplicate = runBlocking { generatePatientRegistrationDto() }.copy(insuranceNumber = validForeigner.insuranceNumber)
        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(duplicate)
        }.run {
            expectStatus(HttpStatusCode.Conflict)
        }
    }

    @Test
    @Suppress("LongMethod") // complete crud, long method is fine here
    fun `complete crud on patient`() = withTestApplication {
        val createSlots = CreateVaccinationSlotsDtoIn(
            locationId = locationId,
            from = Instant.ofEpochMilli(20),
            to = Instant.ofEpochMilli(30),
            durationMillis = 10,
            bandwidth = 1,
        )
        val slotService by closestDI().instance<VaccinationSlotService>()
        val slots = runBlocking { slotService.addSlots(createSlots) }
        assertEquals(1, slots.size)

        val validRegistration = runBlocking { generatePatientRegistrationDto() }
        // create patient
        val patientId = handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(validRegistration)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val registration = receive<PatientRegistrationResponseDtoOut>()
            assertEquals(slots.single(), registration.slot.id)
            registration.patientId
        }

        // get patient by personal number
        handleRequest(HttpMethod.Get, "${Routes.adminSectionPatient}?personalOrInsuranceNumber=${validRegistration.personalNumber}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val patient = receive<PatientDtoOut>()
            assertEquals(patientId, patient.id)
            assertEquals(validRegistration.personalNumber, patient.personalNumber)
            assertEquals(validRegistration.email, patient.email)
        }

        // get patient by ID
        handleRequest(HttpMethod.Get, "${Routes.adminSectionPatient}/$patientId") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val patient = receive<PatientDtoOut>()
            assertEquals(patientId, patient.id)
            assertEquals(validRegistration.personalNumber, patient.personalNumber)
            assertEquals(validRegistration.email, patient.email)
        }

        // try to get patient id by the email
        handleRequest(HttpMethod.Get, "${Routes.adminSectionPatient}/filter?email=${validRegistration.email}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val patients = receive<List<PatientDtoOut>>()
            assertEquals(1, patients.size)
            assertEquals(patientId, patients.single().id)
        }

        // update email address
        val newEmailAddress = "${UUID.randomUUID()}@mild.blue"
        handleRequest(HttpMethod.Put, "${Routes.adminSectionPatient}/$patientId") {
            authorize()
            jsonBody(PatientUpdateDtoIn(email = newEmailAddress))
        }.run {
            expectStatus(HttpStatusCode.OK)
            val patient = receive<PatientDtoOut>()
            assertEquals(patientId, patient.id)
            assertEquals(validRegistration.personalNumber, patient.personalNumber)
            assertEquals(newEmailAddress, patient.email)
        }

        // delete patient
        handleRequest(HttpMethod.Delete, "${Routes.adminSectionPatient}/$patientId") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
        }

        // no patient should show up on filter search
        handleRequest(HttpMethod.Get, "${Routes.adminSectionPatient}/filter") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val patients = receive<List<PatientDtoOut>>()
            assertTrue { patients.isEmpty() }
        }

        // the patient is no longer in db
        handleRequest(HttpMethod.Get, "${Routes.adminSectionPatient}/$patientId") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.NotFound)
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
            bandwidth = 10,
        )

        val vaccinationSlotService by closestDI().instance<VaccinationSlotService>()
        val totalSlots = runBlocking { vaccinationSlotService.addSlots(createSlots) }.size
        val cachedQuestions = transaction {
            Questions.selectAll().map { AnswerDtoIn(it[Questions.id], true) }
        }

        val bookedSlots = runBlocking {
            (0 until totalSlots).map {
                async {
                    handleRequest(HttpMethod.Post, "${Routes.patient}?captcha=disabled") {
                        jsonBody(generatePatientRegistrationDto(cachedQuestions))
                    }.run {
                        expectStatus(HttpStatusCode.OK)
                        receive<PatientRegistrationResponseDtoOut>()
                    }
                }
            }.awaitAll()
        }

        assertEquals(totalSlots, bookedSlots.size)
        // check that each slot was booked just once
        bookedSlots.groupBy({ it.slot.id }, { it.patientId })
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


    @Test
    fun `patient should not be registered due to the ISIN rejection`() = withTestApplication {
        val createSlots = CreateVaccinationSlotsDtoIn(
            locationId = locationId,
            from = Instant.ofEpochMilli(20),
            to = Instant.ofEpochMilli(30),
            durationMillis = 10,
            bandwidth = 1,
        )
        val slotService by closestDI().instance<VaccinationSlotService>()
        val slots = runBlocking { slotService.addSlots(createSlots) }
        assertEquals(1, slots.size)

        val validRegistration = runBlocking { generatePatientRegistrationDto() }

        val validationService by closestDI().instance<PatientValidationService>()
        // force retrieve delegate by calling random function, otherwise mockk magic won't work
        println(validationService.hashCode())

        coEvery {
            validationService.validatePatient(
                validRegistration.firstName,
                validRegistration.lastName,
                validRegistration.personalNumber ?: ""
            )
        } returns IsinValidationResultDto(PatientValidationResult.PATIENT_NOT_FOUND)

        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(validRegistration)
        }.run {
            expectStatus(HttpStatusCode.NotAcceptable)
        }
    }

    @Test
    fun `patient should be registered`() = withTestApplication {
        val createSlots = CreateVaccinationSlotsDtoIn(
            locationId = locationId,
            from = Instant.ofEpochMilli(20),
            to = Instant.ofEpochMilli(30),
            durationMillis = 10,
            bandwidth = 1,
        )
        val slotService by closestDI().instance<VaccinationSlotService>()
        val slots = runBlocking { slotService.addSlots(createSlots) }
        assertEquals(1, slots.size)

        val validRegistration = runBlocking { generatePatientRegistrationDto() }

        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(validRegistration)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val registration = receive<PatientRegistrationResponseDtoOut>()
            assertEquals(slots.single(), registration.slot.id)
        }
    }

    @Test
    fun `patient should not be registered because all slots are booked`() = withTestApplication {
        val createSlots = CreateVaccinationSlotsDtoIn(
            locationId = locationId,
            from = Instant.ofEpochMilli(20),
            to = Instant.ofEpochMilli(30),
            durationMillis = 10,
            bandwidth = 1,
        )
        val slotService by closestDI().instance<VaccinationSlotService>()
        val slots = runBlocking { slotService.addSlots(createSlots) }
        assertEquals(1, slots.size)

        val validRegistration = runBlocking { generatePatientRegistrationDto() }
        // this is ok as there's one available slot
        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(validRegistration)
        }.run {
            expectStatus(HttpStatusCode.OK)
        }
        // now the patient should not be registered because there are no slots left
        val anotherValidRequest = runBlocking { generatePatientRegistrationDto() }
        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(anotherValidRequest)
        }.run {
            expectStatus(HttpStatusCode.NotFound)
        }
        // and verify that the patient does not exist in the database
        val anotherPatientExists = transaction {
            Patients.select { Patients.personalNumber eq anotherValidRequest.personalNumber }.count() != 0L
        }
        assertFalse { anotherPatientExists }
    }

    @Test
    fun `patient should not be able to register twice`() = withTestApplication {
        val createSlots = CreateVaccinationSlotsDtoIn(
            locationId = locationId,
            from = Instant.ofEpochMilli(20),
            to = Instant.ofEpochMilli(30),
            durationMillis = 10,
            bandwidth = 2,
        )
        val slotService by closestDI().instance<VaccinationSlotService>()
        val slots = runBlocking { slotService.addSlots(createSlots) }
        assertEquals(2, slots.size)

        val validRegistration = runBlocking { generatePatientRegistrationDto() }
        // first registration
        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(validRegistration)
        }.run {
            expectStatus(HttpStatusCode.OK)
        }
        // second registration
        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(validRegistration)
        }.run {
            expectStatus(HttpStatusCode.Conflict)
        }
    }

    @Test
    fun `should reject registration due to no slots available`() = withTestApplication {
        val validRegistration = runBlocking { generatePatientRegistrationDto() }
        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(validRegistration)
        }.run {
            expectStatus(HttpStatusCode.NotFound)
        }
    }

    @Test
    fun `try to register patient with invalid data`() = withTestApplication {
        val validRegistration = runBlocking { generatePatientRegistrationDto() }

        val invalidEmail = validRegistration.copy(email = "notvalidmail.com")
        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(invalidEmail)
        }.run {
            expectStatus(HttpStatusCode.BadRequest)
        }

        val invalidPersonalNumber = validRegistration.copy(personalNumber = "123456789")
        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(invalidPersonalNumber)
        }.run {
            expectStatus(HttpStatusCode.BadRequest)
        }

        val invalidPhoneNumber = validRegistration.copy(phoneNumber = PhoneNumberDtoIn("12345879", "CZ"))
        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(invalidPhoneNumber)
        }.run {
            expectStatus(HttpStatusCode.BadRequest)
        }

        val incompleteAnswers = validRegistration.copy(answers = validRegistration.answers.dropLast(1))
        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(incompleteAnswers)
        }.run {
            expectStatus(HttpStatusCode.BadRequest)
        }

        val invalidAnswerId =
            validRegistration.copy(answers = validRegistration.answers.dropLast(1) + AnswerDtoIn(UUID.randomUUID(), true))
        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(invalidAnswerId)
        }.run {
            expectStatus(HttpStatusCode.BadRequest)
        }

        val wrongConfirmation = validRegistration.copy(
            confirmation = ConfirmationDtoIn(
                healthStateDisclosureConfirmation = true,
                covid19VaccinationAgreement = true,
                gdprAgreement = false
            )
        )
        handleRequest(HttpMethod.Post, patientRoute) {
            jsonBody(wrongConfirmation)
        }.run {
            expectStatus(HttpStatusCode.BadRequest)
        }
    }
}
