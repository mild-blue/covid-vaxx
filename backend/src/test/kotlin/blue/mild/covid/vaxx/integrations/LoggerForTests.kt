package blue.mild.covid.vaxx.integrations

import blue.mild.covid.vaxx.integrations.platform.RequestMetric
import blue.mild.covid.vaxx.integrations.platform.requestDurationInSeconds
import blue.mild.covid.vaxx.utils.createLogger

private val logger = createLogger("Test")

fun logResults(results: List<RequestMetric>) {
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
