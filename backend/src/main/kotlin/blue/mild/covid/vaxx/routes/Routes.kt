package blue.mild.covid.vaxx.routes

/**
 * All routes in the application.
 */
object Routes {
    val patient = apiName("patient")

    val questions = apiName("questions")

    val insuranceCompanies = apiName("insurance-companies")

    val version = apiName("version")
    val status = apiName("status")
    val statusHealth = "$status/health"

    val systemStatistics = adminRoute("statistics")
    val runIsinJob = adminRoute("run-isin-job")

    val registeredUserLogin = adminRoute("login")
    val userRegistration = adminRoute("register")
    val userLoginVerification = adminRoute("self")

    val publicLocations = apiName("locations")

    val locations = adminRoute("locations")

    val vaccinationSlots = adminRoute("vaccination-slots")

    val adminSectionPatient = adminRoute("patient")
    val vaccination = adminRoute("vaccination")
    val nurse = adminRoute("nurse")

    val dataCorrectness = adminRoute("data-correctness")

    val cacheRefresh = adminRoute("cache-refresh")

    const val openApiJson = "/openapi.json"
    const val swaggerUi = "/swagger-ui"

    private fun adminRoute(name: String) = apiName("admin/$name")
    private fun apiName(name: String) = "/api/$name"
}


