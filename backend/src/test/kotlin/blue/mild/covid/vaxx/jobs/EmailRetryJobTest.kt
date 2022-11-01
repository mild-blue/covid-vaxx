package blue.mild.covid.vaxx.jobs

import blue.mild.covid.vaxx.dao.repository.PatientRepository
import blue.mild.covid.vaxx.dto.internal.PatientEmailRequestDto
import blue.mild.covid.vaxx.dto.request.CreateVaccinationSlotsDtoIn
import blue.mild.covid.vaxx.dto.request.LocationDtoIn
import blue.mild.covid.vaxx.dto.request.PhoneNumberDtoIn
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import blue.mild.covid.vaxx.generators.generatePatientInDatabase
import blue.mild.covid.vaxx.service.LocationService
import blue.mild.covid.vaxx.service.MailService
import blue.mild.covid.vaxx.service.VaccinationSlotService
import blue.mild.covid.vaxx.utils.DatabaseTestBase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.instance
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EmailRetryJobTest : DatabaseTestBase() {

    private lateinit var patient1: PatientDtoOut
    private lateinit var patient2: PatientDtoOut
    private lateinit var patient3: PatientDtoOut

    override fun populateDatabase(di: DI) {
        val locationService by di.instance<LocationService>()
        val location = LocationDtoIn(
            address = "AAAA",
            zipCode = 16000,
            district = "AAAA",
            phoneNumber = PhoneNumberDtoIn("+420724123456", "CZ"),
            email = "AAAA@test.com",
            notes = "AAAA - note"
        )
        val locationId = runBlocking { locationService.addLocation(location) }

        val createSlots = CreateVaccinationSlotsDtoIn(
            locationId = locationId,
            from = Instant.ofEpochMilli(20),
            to = Instant.ofEpochMilli(30),
            durationMillis = 10,
            bandwidth = 10,
        )
        val slotService by di.instance<VaccinationSlotService>()
        val slots = runBlocking { slotService.addSlots(createSlots) }
        assertEquals(10, slots.size)

        val patientRepository by di.instance<PatientRepository>()
        runBlocking {
            patient1 = patientRepository.generatePatientInDatabase()
            patient2 = patientRepository.generatePatientInDatabase()
            patient3 = patientRepository.generatePatientInDatabase()
        }
    }

    @Test
    fun `job should resend email only to patients with slots and no emails`() {
        val locationService by rootDI.instance<LocationService>()
        val slotService by rootDI.instance<VaccinationSlotService>()
        val patientRepository by rootDI.instance<PatientRepository>()

        // book slot for the first patient
        // now this guy didnt get the email but has vaccination slot
        runBlocking { slotService.bookSlotForPatient(patient1.id) }
        // second guy has slot AND the email was sent
        runBlocking { slotService.bookSlotForPatient(patient2.id) }
        assertTrue {
            runBlocking {
                patientRepository.updatePatientChangeSet(
                    id = patient2.id,
                    registrationEmailSent = Instant.EPOCH
                )
            }
        }
        // only the third patient should get the email
        patient2 = runBlocking {
            assertNotNull(patientRepository.getAndMapById(patientId = patient2.id))
        }

        val mailServiceMock = mockk<MailService>()
        val slot = slot<PatientEmailRequestDto>()
        coEvery { mailServiceMock.sendEmail(capture(slot)) } just Runs

        val job = EmailRetryJob(mailServiceMock, locationService)
        // execute the job
        job.execute()
        coVerify(exactly = 1) { mailServiceMock.sendEmail(any()) }
        assertTrue { slot.isCaptured }
        assertEquals(patient1.id, slot.captured.patientId)

        // register that the email was sent for patient 1
        assertTrue {
            runBlocking {
                patientRepository.updatePatientChangeSet(
                    id = patient1.id,
                    registrationEmailSent = Instant.EPOCH
                )
            }
        }

        // another execution should not do anything, so the method sendEmail should
        // still be called just once
        job.execute()
        coVerify(exactly = 1) { mailServiceMock.sendEmail(any()) }
    }
}
