package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dao.model.VaccinationBodyPart
import blue.mild.covid.vaxx.dao.repository.PatientRepository
import blue.mild.covid.vaxx.dao.repository.VaccinationRepository
import blue.mild.covid.vaxx.dto.request.IsinJobDtoIn
import blue.mild.covid.vaxx.dto.request.UserRegistrationDtoIn
import blue.mild.covid.vaxx.dto.response.ApplicationInformationDtoOut
import blue.mild.covid.vaxx.dto.response.IsinJobDtoOut
import blue.mild.covid.vaxx.dto.response.SystemStatisticsDtoOut
import blue.mild.covid.vaxx.service.UserService
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

class ServiceRoutesTest : ServerTestBase() {

    private val versionDto = ApplicationInformationDtoOut("test")

    // an example of overriding the DI container
    override fun overrideDIContainer() = DI {
        bind<ApplicationInformationDtoOut>() with singleton { versionDto }
    }

    @Test
    fun `status should respond with ok`() = withTestApplication {
        handleRequest(HttpMethod.Get, Routes.status).run {
            expectStatus(HttpStatusCode.OK)
        }
    }

    @Test
    fun `version should respond with correct version`() = withTestApplication {
        handleRequest(HttpMethod.Get, Routes.version).run {
            expectStatus(HttpStatusCode.OK)
            // expect DTO that was injected directly from this test
            assertEquals(versionDto, receive())
        }
    }

    @Test
    fun `system analytics should respond with correct data`() = withTestApplication {
        val expected = SystemStatisticsDtoOut(0, 0, 0, 0, 0, 0)
        handleRequest(HttpMethod.Get, Routes.systemStatistics).run {
            expectStatus(HttpStatusCode.OK)
            assertEquals(expected, receive())
        }
    }

    @Test
    @Suppress("LongMethod")
    fun `it should rerun isin job correctly even if patient has no answers`() = withTestApplication {
        val patientRepository by closestDI().instance<PatientRepository>()
        val vaccinationRepository by closestDI().instance<VaccinationRepository>()
        val userService by closestDI().instance<UserService>()

        val patientId = runBlocking {
            patientRepository.savePatient(
                "alice",
                "alice",
                12345,
                "alice",
                "1",
                "1",
                null,
                "email",
                InsuranceCompany.CPZP,
                "indication",
                "remoteHost",
                mapOf(),
                null
            )
        }

        val userId = runBlocking {
            userService.registerUser(
                UserRegistrationDtoIn(
                    firstName = "Test",
                    lastName = "test",
                    email = "Test",
                    password = "test",
                    role = UserRole.ADMIN
                )
            )
        }

        runBlocking {
            vaccinationRepository.addVaccination(
                patientId,
                bodyPart = VaccinationBodyPart.BUTTOCK,
                vaccinatedOn = Instant.now(),
                vaccineSerialNumber = "test",
                vaccineExpiration = LocalDate.parse(
                    "2200-01-01",
                    DateTimeFormatter.ISO_LOCAL_DATE
                ),
                userPerformingVaccination = userId.id,
                doseNumber = 2
            )
        }

        val expected = IsinJobDtoOut(
            0, 1, 0, 0,
            0, 0, 0,
            0, 0, 0
        )
        val runWhat = IsinJobDtoIn(
            exportPatientsInfo = true,
            checkVaccinations = true,
            exportVaccinationsFirstDose = true,
            exportVaccinationsSecondDose = true,
            validatePatients = true
        )

        handleRequest(HttpMethod.Post, Routes.runIsinJob) {
            authorize()
            jsonBody(runWhat)
        }.run {
            expectStatus(HttpStatusCode.OK)
            val registration = receive<IsinJobDtoOut>()
            assertEquals(expected, registration)
        }
    }
}
