package blue.mild.covid.vaxx.security

import blue.mild.covid.vaxx.routes.Routes
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CspHeadersAreExposedForSwagger : ServerTestBase(needsDatabase = false) {

    @Test
    fun `test server has CSP set when accessing swagger UI`() = withTestApplication {
        val swaggerCspHeader = "default-src 'self';connect-src 'self';media-src data:;img-src 'self' data:;" +
                "style-src 'self' 'unsafe-inline';script-src 'self' 'unsafe-inline'"

        val routesWithSwaggerCSP = listOf(
            Routes.openApiJson,
            "${Routes.swaggerUi}/index.html",
            "${Routes.swaggerUi}/index.html?url=${Routes.openApiJson}",
            "${Routes.swaggerUi}/swagger-ui-bundle.js",
            "${Routes.swaggerUi}/swagger-ui-standalone-preset.js"
        )

        routesWithSwaggerCSP.forEach { route ->
            val response = runBlocking { client.get(route) }
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(swaggerCspHeader, response.headers["Content-Security-Policy"])
        }
    }
}
