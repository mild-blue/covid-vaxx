package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dao.model.VaccinationBodyPart
import blue.mild.covid.vaxx.dao.repository.PatientRepository
import blue.mild.covid.vaxx.dto.request.VaccinationDtoIn
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import blue.mild.covid.vaxx.dto.response.VaccinationDetailDtoOut
import blue.mild.covid.vaxx.generators.generatePatientInDatabase
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.instance
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.assertEquals

class VaccinationRouteTest : ServerTestBase() {

    private lateinit var patient1: PatientDtoOut
    private lateinit var patient2: PatientDtoOut

    override fun populateDatabase(di: DI): Unit = runBlocking {
        val patientRepository by di.instance<PatientRepository>()

        patient1 = patientRepository.generatePatientInDatabase()
        patient2 = patientRepository.generatePatientInDatabase()
    }

    @Suppress("LongMethod") // This is test, it is ok here
    @Test
    fun `test vaccination flow`() = withTestApplication {
        // verify that only authorized users can access vaccination data
        handleRequest(HttpMethod.Get, "${Routes.vaccination}?id=${patient1.id}").run {
            expectStatus(HttpStatusCode.Unauthorized)
        }

        // verify that there's no vaccinations for patient1
        handleRequest(HttpMethod.Get, "${Routes.vaccination}?id=${patient1.id}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.NotFound)
        }

        // create vaccination first dose
        val inputFirstDose = VaccinationDtoIn(
            patientId = patient1.id,
            bodyPart = VaccinationBodyPart.BUTTOCK,
            vaccinatedOn = Instant.EPOCH.plus(10, ChronoUnit.DAYS),
            notes = "ok",
            doseNumber = 1
        )
        val vaccinationFirstDoseId = handleRequest(HttpMethod.Post, Routes.vaccination) {
            authorize()
            jsonBody(inputFirstDose)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val output = receive<VaccinationDetailDtoOut>()
            assertEquals(defaultPrincipal.userId, output.doctor.id)
            assertEquals(defaultPrincipal.nurseId, output.nurse?.id)
            assertEquals(defaultPrincipal.vaccineSerialNumber, output.vaccineSerialNumber)
            assertEquals(inputFirstDose.notes, output.notes)
            assertEquals(inputFirstDose.patientId, output.patientId)
            assertEquals(inputFirstDose.bodyPart, output.bodyPart)
            assertEquals(inputFirstDose.vaccinatedOn, output.vaccinatedOn)
            assertEquals(inputFirstDose.doseNumber, output.doseNumber)
            output.vaccinationId
        }

        // verify that getting vaccination first dose by vaccination id works
        handleRequest(HttpMethod.Get, "${Routes.vaccination}/$vaccinationFirstDoseId") {
            // authorize with different user then the one that created the vaccination
            authorize(
                UserPrincipal(
                    userId = UUID.randomUUID(),
                    userRole = UserRole.DOCTOR,
                    vaccineSerialNumber = "",
                    vaccineExpiration = LocalDate.now()
                )
            )
        }.run {
            expectStatus(HttpStatusCode.OK)
            val output = receive<VaccinationDetailDtoOut>()
            assertEquals(defaultPrincipal.userId, output.doctor.id)
            assertEquals(defaultPrincipal.nurseId, output.nurse?.id)
            assertEquals(defaultPrincipal.vaccineSerialNumber, output.vaccineSerialNumber)
            assertEquals(inputFirstDose.notes, output.notes)
            assertEquals(inputFirstDose.patientId, output.patientId)
            assertEquals(inputFirstDose.bodyPart, output.bodyPart)
            assertEquals(inputFirstDose.vaccinatedOn, output.vaccinatedOn)
            assertEquals(inputFirstDose.doseNumber, output.doseNumber)

            assertEquals(patient1.id, output.patientId)
        }

        // create vaccination second dose
        val inputSecondDose = VaccinationDtoIn(
            patientId = patient1.id,
            bodyPart = VaccinationBodyPart.NON_DOMINANT_HAND,
            vaccinatedOn = Instant.EPOCH.plus(20, ChronoUnit.DAYS),
            notes = "ok",
            doseNumber = 2
        )
        val vaccinationSecondDoseId = handleRequest(HttpMethod.Post, Routes.vaccination) {
            authorize()
            jsonBody(inputSecondDose)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val output = receive<VaccinationDetailDtoOut>()
            assertEquals(defaultPrincipal.userId, output.doctor.id)
            assertEquals(defaultPrincipal.nurseId, output.nurse?.id)
            assertEquals(defaultPrincipal.vaccineSerialNumber, output.vaccineSerialNumber)
            assertEquals(inputSecondDose.notes, output.notes)
            assertEquals(inputSecondDose.patientId, output.patientId)
            assertEquals(inputSecondDose.bodyPart, output.bodyPart)
            assertEquals(inputSecondDose.vaccinatedOn, output.vaccinatedOn)
            assertEquals(inputSecondDose.doseNumber, output.doseNumber)
            output.vaccinationId
        }

        // verify that getting vaccination second dose by vaccination id works
        handleRequest(HttpMethod.Get, "${Routes.vaccination}/$vaccinationSecondDoseId") {
            // authorize with different user then the one that created the vaccination
            authorize(
                UserPrincipal(
                    userId = UUID.randomUUID(),
                    userRole = UserRole.DOCTOR,
                    vaccineSerialNumber = "",
                    vaccineExpiration = LocalDate.now()
                )
            )
        }.run {
            expectStatus(HttpStatusCode.OK)
            val output = receive<VaccinationDetailDtoOut>()
            assertEquals(defaultPrincipal.userId, output.doctor.id)
            assertEquals(defaultPrincipal.nurseId, output.nurse?.id)
            assertEquals(defaultPrincipal.vaccineSerialNumber, output.vaccineSerialNumber)
            assertEquals(inputSecondDose.notes, output.notes)
            assertEquals(inputSecondDose.patientId, output.patientId)
            assertEquals(inputSecondDose.bodyPart, output.bodyPart)
            assertEquals(inputSecondDose.vaccinatedOn, output.vaccinatedOn)
            assertEquals(inputSecondDose.doseNumber, output.doseNumber)

            assertEquals(patient1.id, output.patientId)
        }

        // verify that there's still no vaccination for patient2 as we were registering patient1
        handleRequest(HttpMethod.Get, "${Routes.vaccination}?id=${patient2.id}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.NotFound)
        }

        // TODO #259 implement me!
        // verify that the patient1 really has the vaccination
        // by calling the API and requesting data for patient1.id and expecting Ok status
        // and then comparing vaccinationId with the ID you received in the test
        // hint: see first part of the test
    }
}
