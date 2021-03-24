package blue.mild.covid.vaxx.utils

import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import blue.mild.covid.vaxx.dto.request.query.CaptchaVerificationDtoIn
import blue.mild.covid.vaxx.dto.response.AnswerDtoOut
import blue.mild.covid.vaxx.dto.response.QuestionDtoOut
import blue.mild.covid.vaxx.platform.PatientRegistrationBuilder
import blue.mild.covid.vaxx.platform.defaultPatientRegistrationBuilder
import blue.mild.covid.vaxx.routes.Routes
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.call.receive
import io.ktor.client.request.accept
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import mu.KLogging
import kotlin.random.Random


abstract class RegistrationApiTestHelper(
    targetHost: String,
    requestTimeoutsSeconds: Int
) : ApiCallTestHelper(targetHost, requestTimeoutsSeconds) {
    private companion object : KLogging()


    protected suspend fun runPatientRegistrationWithBuilder(
        registrationBuilder: PatientRegistrationBuilder = defaultPatientRegistrationBuilder(),
        maybeInsuranceCompany: Any? = null,
        maybeAnswers: Any? = null,
        httpStatus: HttpStatusCode = HttpStatusCode.OK,
        onDone: (requestNumber: Int) -> Unit
    ) {
        runPatientRegistrationWithBuilder(
            registrationBuilder,
            maybeInsuranceCompany,
            maybeAnswers,
            httpStatus = httpStatus
        )
        onDone(counter.incrementAndGet())
    }

    private suspend fun runPatientRegistrationWithBuilder(
        registrationBuilder: PatientRegistrationBuilder,
        maybeInsuranceCompany: Any? = null,
        maybeAnswers: Any? = null,
        httpStatus: HttpStatusCode = HttpStatusCode.OK
    ) {
        var request = loadClientSource()
        require(request.status.isSuccess()) { "It was not possible to load client sources ${request.status.description}" }

        val insuranceCompany = maybeInsuranceCompany ?: run {
            request = getInsuranceCompanies()
            require(request.status.isSuccess()) { "It was not possible to load client sources ${request.status.description}" }
            request.receive<JsonNode>()
            InsuranceCompany.values().random()
        }

        val answers = maybeAnswers ?: run {
            request = getQuestions()
            require(request.status.isSuccess()) { "Questions request was not successful. ${request.status.description}" }
            val questions = request.receive<List<QuestionDtoOut>>()
            questions.map { AnswerDtoOut(it.id, Random.nextBoolean()) }
        }
        // register patient
        request = registerPatient(
            registrationBuilder(
                answers, insuranceCompany
            )
        )

        require(request.status == httpStatus) {
            "Patient registration did not run as expected " +
                    "expected was status code $httpStatus but got ${request.status.value} ${request.status.description}" +
                    "${request.receive<JsonNode>()}"
        }
    }

    private suspend fun registerPatient(patient: PatientRegistrationDtoInForTests) =
        meteredClient.post<HttpResponse>("${targetHost}${Routes.patient}") {
            parameter(CaptchaVerificationDtoIn.NAME, "1234")
            contentType(ContentType.Application.Json)
            accept(ContentType.Companion.Any)
            body = patient
        }
}
