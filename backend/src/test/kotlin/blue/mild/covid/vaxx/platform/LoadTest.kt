package blue.mild.covid.vaxx.platform

import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dto.AnswerDto
import blue.mild.covid.vaxx.dto.request.CaptchaVerificationDtoIn
import blue.mild.covid.vaxx.dto.request.ConfirmationDtoIn
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.dto.response.QuestionDtoOut
import blue.mild.covid.vaxx.routes.Routes
import blue.mild.covid.vaxx.utils.createLogger
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.Json
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import mu.KLogging
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

abstract class LoadTest(
    private val targetHost: String,
    private val requestTimeoutsSeconds: Int
) {
    private companion object : KLogging()

    protected val callsCollection: MutableCollection<RequestMetric> = ConcurrentLinkedQueue()

    private val counter = AtomicInteger(0)

    /**
     * Debug logger for HTTP Requests.
     */
    private val Logger.Companion.debug: Logger
        get() = object : Logger, org.slf4j.Logger by createLogger("DebugHttpClient") {
            override fun log(message: String) {
                debug(message)
            }
        }

    private val meteredClient by lazy {
        HttpClient(Apache) {
            Json {
                serializer = JacksonSerializer {
                    registerModule(JavaTimeModule())
                }
            }

            install(ClientRequestMetric) {
                onResponse {
                    callsCollection.add(it)
                }
            }

            install(Logging) {
                logger = Logger.debug
                level = LogLevel.ALL
            }

            install(HttpTimeout) {
                requestTimeoutMillis = requestTimeoutsSeconds * 1000L
            }
        }
    }

    abstract suspend fun execute(): List<RequestMetric>

    protected suspend fun runPatientRegistrationWithBuilder(
        registrationBuilder: PatientRegistrationBuilder,
        onDone: (requestNumber: Int) -> Unit
    ) {
        runCatching {
            runPatientRegistrationWithBuilder(registrationBuilder)
        }.onFailure {
            logger.error { it.message }
        }
        onDone(counter.incrementAndGet())
    }

    private suspend fun runPatientRegistrationWithBuilder(registrationBuilder: PatientRegistrationBuilder) {
        var request = loadClientSource()
        require(request.status.isSuccess()) { "It was not possible to load client sources ${request.status.description}" }

        // get insurance companies
        request = getInsuranceCompanies()
        require(request.status.isSuccess()) { "Insurance companies request was not successful. ${request.status.description}" }
        // we don't really have correct dto that can be deserialized
        request.receive<JsonNode>()

        // get questions
        request = getQuestions()
        require(request.status.isSuccess()) { "Questions request was not successful. ${request.status.description}" }
        val questions = request.receive<List<QuestionDtoOut>>()
        val answers = questions.map { AnswerDto(it.id, Random.nextBoolean()) }

        // register patient
        request = registerPatient(
            registrationBuilder(
                answers, InsuranceCompany.values().random(), ConfirmationDtoIn(
                    healthStateDisclosureConfirmation = true,
                    covid19VaccinationAgreement = true,
                    gdprAgreement = true
                )
            )
        )
        require(request.status.isSuccess()) { "Patient registration was not successful. ${request.status.description}" }
    }

    private suspend fun loadClientSource() =
        meteredClient.get<HttpResponse>(targetHost)

    private suspend fun getInsuranceCompanies() =
        meteredClient.get<HttpResponse>("${targetHost}${Routes.insuranceCompanies}")

    private suspend fun getQuestions() =
        meteredClient.get<HttpResponse>("${targetHost}${Routes.questions}")

    private suspend fun registerPatient(patient: PatientRegistrationDtoIn) =
        meteredClient.post<HttpResponse>("${targetHost}${Routes.patient}") {
            parameter(CaptchaVerificationDtoIn.NAME, "1234") // should be disabled
            contentType(ContentType.Application.Json)
            accept(ContentType.Companion.Any)
            body = patient
        }
}
