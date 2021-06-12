package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dto.response.ApplicationInformationDtoOut
import blue.mild.covid.vaxx.dto.response.SystemStatisticsDtoOut
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.bind
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
}
