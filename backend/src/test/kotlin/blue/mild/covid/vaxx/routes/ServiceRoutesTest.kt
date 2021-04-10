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
    fun `test status responds ok`() = withTestApplication {
        handleRequest(HttpMethod.Get, Routes.status).run {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `test version is correct`() = withTestApplication {
        handleRequest(HttpMethod.Get, Routes.version).run {
            assertEquals(HttpStatusCode.OK, response.status())
            // expect DTO that was injected directly from this test
            assertEquals(versionDto, receive())
        }
    }

    @Test
    fun `test system analytics`() = withTestApplication {
        handleRequest(HttpMethod.Get, Routes.systemStatistics).run {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }

        val expected = SystemStatisticsDtoOut(0, 0, 0, 0)
        handleRequest(HttpMethod.Get, Routes.systemStatistics) { authorize() }.run {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(expected, receive())
        }
    }
}
