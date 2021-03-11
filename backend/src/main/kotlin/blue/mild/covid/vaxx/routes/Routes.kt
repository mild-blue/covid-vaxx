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
    val statusHealth = apiName("status/health")

    val registeredUserLogin = apiName("admin/login")
    val userRegistration = apiName("admin/register")
    val userLoginVerification = apiName("admin/self")
    val adminSectionPatient = apiName("admin/patient")
    val cacheRefresh = apiName("admin/cache-refresh")

    const val openApiJson = "/openapi.json"
    const val swaggerUi = "/swagger-ui"

    private fun apiName(name: String) = "/api/$name"
}


