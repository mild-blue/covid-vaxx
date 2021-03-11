package blue.mild.covid.vaxx.api

import blue.mild.covid.vaxx.utils.logResults
import kotlinx.coroutines.runBlocking

private const val TARGET_HOST = "https://covid-vaxx.stg.mild.blue"

/**
 * Execute tests testing API calls with valid and invalid data
 */
fun main() {
    listOf(
        registrationApiTest(),
        loginApiTest()
    ).map {
        runBlocking { it.execute() }.also(::logResults)
    }
}
private fun registrationApiTest() = RegistrationApiTest(
    targetHost = TARGET_HOST
)

private fun loginApiTest() = LoginApiTest(
    targetHost = TARGET_HOST
)



