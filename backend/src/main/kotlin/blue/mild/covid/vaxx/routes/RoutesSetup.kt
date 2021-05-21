package blue.mild.covid.vaxx.routes

import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute

/**
 * Register all routes in the application.
 */
fun NormalOpenAPIRoute.registerRoutes() {
    patientRoutes()
    questionRoutes()
    insuranceCompanyRoutes()
    userRoutes()
    locationRoutes()
    vaccinationSlotRoutes()
    serviceRoutes()
    vaccinationRoutes()
    dataCorrectnessRoutes()
    nurseRoutes()
}
