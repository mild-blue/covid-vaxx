package blue.mild.covid.vaxx.routes

object Routes {
    val patient = apiName("patient")
    val question = apiName("question")
    val insuranceCompanies = apiName("insurance-companies")

    val version = apiName("version")
    val status = apiName("status")
    val statusHealth = apiName("status/health")

    const val openApiJson = "/openapi.json"
    const val swaggerUi = "/swagger-ui"

    private fun apiName(name: String) = "/api/$name"
}


