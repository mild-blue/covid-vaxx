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

    @Test
    @Suppress("LongMethod")
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

        val input = VaccinationDtoIn(
            patientId = patient1.id,
            bodyPart = VaccinationBodyPart.BUTTOCK,
            vaccinatedOn = Instant.EPOCH.plus(10, ChronoUnit.DAYS),
            notes = "ok"
        )
        // create vaccination
        val vaccinationId = handleRequest(HttpMethod.Post, Routes.vaccination) {
            authorize()
            jsonBody(input)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val output = receive<VaccinationDetailDtoOut>()
            assertEquals(defaultPrincipal.userId, output.doctor.id)
            assertEquals(defaultPrincipal.nurseId, output.nurse?.id)
            assertEquals(defaultPrincipal.vaccineSerialNumber, output.vaccineSerialNumber)
            assertEquals(input.notes, output.notes)
            assertEquals(input.patientId, output.patientId)
            assertEquals(input.bodyPart, output.bodyPart)
            assertEquals(input.vaccinatedOn, output.vaccinatedOn)
            output.vaccinationId
        }

        // verify that getting correctness by correctness id works
        handleRequest(HttpMethod.Get, "${Routes.vaccination}/$vaccinationId") {
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
            assertEquals(input.notes, output.notes)
            assertEquals(input.patientId, output.patientId)
            assertEquals(input.bodyPart, output.bodyPart)
            assertEquals(input.vaccinatedOn, output.vaccinatedOn)

            assertEquals(patient1.id, output.patientId)
        }

        // verify that there's still no vaccination for patient2 as we were registering patient1
        handleRequest(HttpMethod.Get, "${Routes.vaccination}?id=${patient2.id}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.NotFound)
        }

        // verify that the patient1 really has the vaccination
        handleRequest(HttpMethod.Get, "${Routes.vaccination}?id=${patient1.id}") {
            authorize()
        }.run {
            expectStatus(HttpStatusCode.OK)
            val output = receive<VaccinationDetailDtoOut>()
            assertEquals(vaccinationId, output.vaccinationId)
        }
    }
}
