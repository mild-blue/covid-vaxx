package blue.mild.covid.vaxx.routes

/**
 * All routes in the application.
 */
object Routes {
    val patient = apiName("patient")
    val question = apiName("question")
    val insuranceCompany = apiName("insurance-company")

    val version = apiName("version")
    val status = apiName("status")
    val statusHealth = apiName("status/health")

    val registeredUserLogin = apiName("login")
    val userRegistration = apiName("register")
    val userLoginVerification = apiName("user/self")

    const val openApiJson = "/openapi.json"
    const val swaggerUi = "/swagger-ui"

    private fun apiName(name: String) = "/api/$name"
}


