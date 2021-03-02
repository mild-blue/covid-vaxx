package blue.mild.covid.vaxx.routes

object Routes {
    val patient = apiName("/patient")
    val question = apiName("/question")

    val version = apiName("/version")
    val status = apiName("/status")
    val statusHealth = apiName("/status/health")
    private fun apiName(name: String) = "/api/$name"
}


