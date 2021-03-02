package blue.mild.covid.vaxx.routes

import io.ktor.routing.Routing

fun Routing.registerRoutes() {
    patientRoutes()
    questionRoutes()

    serviceRoutes()
    staticContentRoutes()
}