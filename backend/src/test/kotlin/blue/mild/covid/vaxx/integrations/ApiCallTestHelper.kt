package blue.mild.covid.vaxx.integrations

import blue.mild.covid.vaxx.integrations.platform.ClientRequestMetric
import blue.mild.covid.vaxx.integrations.platform.RequestMetric
import blue.mild.covid.vaxx.routes.Routes
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.jackson.jackson
import mu.KLogging
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

abstract class ApiCallTestHelper(
    protected val targetHost: String,
    private val requestTimeoutsSeconds: Int
) {
    private companion object : KLogging()

    protected val callsCollection: MutableCollection<RequestMetric> = ConcurrentLinkedQueue()

    protected val counter = AtomicInteger(0)

    protected val meteredClient by lazy {
        HttpClient(Apache) {
            install(ContentNegotiation) {
                jackson { registerModule(JavaTimeModule()) }
            }
            install(ClientRequestMetric) {
                onResponse {
                    callsCollection.add(it)
                }
            }

            install(HttpTimeout) {
                requestTimeoutMillis = requestTimeoutsSeconds * 1000L
            }
            expectSuccess = false
        }
    }

    abstract suspend fun execute(): List<RequestMetric>

    protected suspend fun loadClientSource() =
        meteredClient.get(targetHost)

    protected suspend fun getInsuranceCompanies() =
        meteredClient.get("${targetHost}${Routes.insuranceCompanies}")

    protected suspend fun getQuestions() =
        meteredClient.get("${targetHost}${Routes.questions}")
}
