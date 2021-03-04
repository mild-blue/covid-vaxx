package blue.mild.covid.vaxx.routes

object Routes {
    val patient = apiName("patient")
    val question = apiName("question")
    val insuranceCompany = apiName("insurance-company")

    val version = apiName("version")
    val status = apiName("status")
    val statusHealth = apiName("status/health")

    val registeredUserLogin = apiName("login")
    val registrationCaptcha = apiName("captcha")
    val self = apiName("self")

    const val openApiJson = "/openapi.json"
    const val swaggerUi = "/swagger-ui"

    private fun apiName(name: String) = "/api/$name"
}


