package blue.mild.covid.vaxx.routes

/**
 * All routes in the application.
 */
object Routes {
    val patient = apiName("patient")

    val question = apiName("question")
    val questionsCacheRefresh = apiName("question/refresh")
    val insuranceCompany = apiName("insurance-company")

    val version = apiName("version")
    val status = apiName("status")
    val statusHealth = apiName("status/health")

    val registeredUserLogin = apiName("admin/login")
    val userRegistration = apiName("admin/register")
    val userLoginVerification = apiName("admin/self")
    val adminSectionPatient = apiName("admin/patient")

    const val openApiJson = "/openapi.json"
    const val swaggerUi = "/swagger-ui"

    private fun apiName(name: String) = "/api/$name"
}


