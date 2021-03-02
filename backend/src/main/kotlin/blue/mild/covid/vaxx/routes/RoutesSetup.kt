package blue.mild.covid.vaxx.routes

import io.ktor.routing.Routing

fun apiName(name: String) = "/api/$name"

fun Routing.registerRoutes() {
    patientRoutes()
    questionRoutes()

    serviceRoutes()
    staticContentRoutes()
}
