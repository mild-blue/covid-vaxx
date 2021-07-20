package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dao.model.Questions
import blue.mild.covid.vaxx.dao.repository.PatientRepository
import blue.mild.covid.vaxx.dto.request.IsinJobDtoIn
import blue.mild.covid.vaxx.dto.response.ApplicationInformationDtoOut
import blue.mild.covid.vaxx.dto.response.IsinJobDtoOut
import blue.mild.covid.vaxx.dto.response.PatientRegistrationResponseDtoOut
import blue.mild.covid.vaxx.dto.response.SystemStatisticsDtoOut
import blue.mild.covid.vaxx.service.QuestionService
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
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
    fun `it should rerun isin job correctly even if patient has no answers`() = withTestApplication {
        val patientRepository by closestDI().instance<PatientRepository>()

        runBlocking {

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

        val expected = IsinJobDtoOut(
            0, 0, 0, 0,
            0, 0, 0,
            0, 0, 0
        )
        val runWhat = IsinJobDtoIn()


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
