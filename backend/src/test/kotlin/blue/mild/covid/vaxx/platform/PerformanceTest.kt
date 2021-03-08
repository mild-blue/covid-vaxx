package blue.mild.covid.vaxx.platform

import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import blue.mild.covid.vaxx.utils.createLogger
import kotlinx.coroutines.runBlocking

private val logger = createLogger("PerformanceTest")

private const val TARGET_HOST = "https://covid-vaxx.stg.mild.blue"
private val CREDENTIALS = LoginDtoIn(
    username = "mildblue",
    password = "bluemild"
)

/**
 * Execute load tests
 */
fun main() {
    listOf(
        timeTest(),
        requestsCountTest()
    ).map {
        runBlocking { it.execute() }.also(::logResults)
    }
}

private fun timeTest() = TimeLoadTest(
    targetHost = TARGET_HOST,
    credentials = CREDENTIALS,
    requestTimeoutsSeconds = 60,
    coroutineWorkers = 1,
    runningTimeInSeconds = 60
)

private fun requestsCountTest() = RoundsLoadTest(
    targetHost = TARGET_HOST,
    credentials = CREDENTIALS,
    requestTimeoutsSeconds = 60,
    coroutineWorkers = 5,
    rounds = 400
)

private fun logResults(results: List<RequestMetric>) {
    val responseTimesInSeconds = results.map { it.requestDurationInSeconds() }
    logger.info { "Average response time: ${responseTimesInSeconds.average()}s" }
    logger.info { "Max time: ${responseTimesInSeconds.maxOrNull()}s" }
    logger.info { "Min time: ${responseTimesInSeconds.minOrNull()}s" }

    val responses = results.groupBy { it.responseCode }
        .map { (code, requests) -> "$code - ${requests.size}" }
        .joinToString("\n")
    logger.info { "Responses:\n$responses" }

    val durationByPath = results.groupBy({ it.url }, { it.requestDurationInSeconds() })
        .map { (url, duration) -> "$url - avg. ${duration.average()}s | min. ${duration.minOrNull()}s | max. ${duration.maxOrNull()}s" }
        .joinToString("\n")
    logger.info { "Duration by path:\n$durationByPath" }
}
