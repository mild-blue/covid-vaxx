package blue.mild.covid.vaxx.platform

import blue.mild.covid.vaxx.dao.InsuranceCompany
import blue.mild.covid.vaxx.dto.AnswerDto
import blue.mild.covid.vaxx.dto.request.ConfirmationDtoIn
import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.dto.response.BearerTokenDtoOut
import blue.mild.covid.vaxx.dto.response.PatientRegisteredDtoOut
import blue.mild.covid.vaxx.dto.response.QuestionDtoOut
import blue.mild.covid.vaxx.routes.Routes
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.Json
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
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
    private val credentials: LoginDtoIn,
    private val requestTimeoutsSeconds: Int
) {
    private companion object : KLogging()

    protected val callsCollection: MutableCollection<RequestMetric> = ConcurrentLinkedQueue()

    private val counter = AtomicInteger(0)

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

        // login
        request = login(credentials)
        require(request.status.isSuccess()) { "Login request was not successful. ${request.status.description}" }
        val (bearerToken) = request.receive<BearerTokenDtoOut>()

        // get insurance companies
        request = getInsuranceCompanies(bearerToken)
        require(request.status.isSuccess()) { "Insurance companies request was not successful. ${request.status.description}" }
        // we don't really have correct dto that can be deserialized
        request.receive<JsonNode>()

        // get questions
        request = getQuestions(bearerToken)
        require(request.status.isSuccess()) { "Questions request was not successful. ${request.status.description}" }
        val questions = request.receive<List<QuestionDtoOut>>()
        val answers = questions.map { AnswerDto(it.id, Random.nextBoolean()) }

        // register patient
        request = registerPatient(
            bearerToken, registrationBuilder(
                answers, InsuranceCompany.values().random(), ConfirmationDtoIn(
                    healthStateDisclosureConfirmation = true,
                    covid19VaccinationAgreement = true
                )
            )
        )
        require(request.status.isSuccess()) { "Patient registration was not successful. ${request.status.description}" }
        request.receive<PatientRegisteredDtoOut>()
    }

    private suspend fun loadClientSource() =
        meteredClient.get<HttpResponse>(targetHost)

    private suspend fun login(loginDto: LoginDtoIn) =
        meteredClient.post<HttpResponse>("${targetHost}${Routes.registeredUserLogin}") {
            contentType(ContentType.Application.Json)
            body = loginDto
        }

    private suspend fun getInsuranceCompanies(bearer: String) =
        meteredClient.get<HttpResponse>("${targetHost}${Routes.insuranceCompany}") {
            authorize(bearer)
        }

    private suspend fun getQuestions(bearer: String) =
        meteredClient.get<HttpResponse>("${targetHost}${Routes.question}") {
            authorize(bearer)
        }

    private suspend fun registerPatient(bearer: String, patient: PatientRegistrationDtoIn) =
        meteredClient.post<HttpResponse>("${targetHost}${Routes.patient}") {
            authorize(bearer)
            contentType(ContentType.Application.Json)
            body = patient
        }

    private fun HttpRequestBuilder.authorize(bearer: String) = header("Authorization", "Bearer $bearer")

}
