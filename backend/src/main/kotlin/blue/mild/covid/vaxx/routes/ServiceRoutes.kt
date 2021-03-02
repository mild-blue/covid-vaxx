package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.DatabaseSetup
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.kodein.di.instance
import org.kodein.di.ktor.di

data class VersionDto(val version: String)

/**
 * Registers prometheus data.
 */
fun Routing.serviceRoutes() {
    val version by di().instance<String>("version")

    /**
     * Send data about version.
     */
    get(apiName("/version")) {
        call.respond(VersionDto(version))
    }

    /**
     * Responds only 200 for ingres.
     */
    get(apiName("/status")) {
        call.respond(HttpStatusCode.OK)
    }

    /**
     * More complex API for indication of all resources.
     */
    get(apiName("/status/health")) {
        if (DatabaseSetup.isConnected()) {
            call.respond(mapOf("health" to "healthy"))
        } else {
            call.respond(HttpStatusCode.ServiceUnavailable, "DB connection is not working")
        }
    }
}
