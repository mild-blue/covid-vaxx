package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dto.response.ApplicationInformationDtoOut
import blue.mild.covid.vaxx.dto.response.SystemStatisticsDtoOut
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import org.junit.jupiter.api.Test
import org.kodein.di.instance
import kotlin.test.assertEquals


class ServiceRoutesTest : ServerTestBase() {
    @Test
    fun `test status responds ok`() = withTestApplication {
        handleRequest(HttpMethod.Get, Routes.status).run {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `test version is correct`() = withTestApplication {
        val expected by di().instance<ApplicationInformationDtoOut>()
        handleRequest(HttpMethod.Get, Routes.version).run {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(expected, receive())
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
