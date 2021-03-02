package blue.mild.covid.vaxx.routes

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.staticContentRoutes() {
    /**
     * Static assets files.
     */
    get("{...}") {
        // TODO assets
        call.respond("some static assets")
    }
}
