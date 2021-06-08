package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.config.IsinConfigurationDto
import blue.mild.covid.vaxx.dto.internal.IsinGetPatientByParametersResultDto
import blue.mild.covid.vaxx.utils.normalizePersonalNumber
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import mu.KLogging
import java.util.Locale


class IsinService(
    private val configuration: IsinConfigurationDto,
    private val isinClient: HttpClient
) {

    private val userIdentification =
        "?pcz=${configuration.pracovnik.pcz}&pracovnikNrzpCislo=${configuration.pracovnik.nrzpCislo}"

    private companion object : KLogging() {
        const val URL_NAJDI_PACIENTA = "pacienti/VyhledatDleJmenoPrijmeniRc"
    }

    suspend fun getPatientByParameters(
        jmeno: String, prijmeni: String, rodneCislo: String
    ): IsinGetPatientByParametersResultDto {
        val firstName = jmeno.trim().uppercase(Locale.getDefault())
        val lastName = prijmeni.trim().uppercase(Locale.getDefault())
        val personalNumber = rodneCislo.normalizePersonalNumber()

        val url = createIsinURL(URL_NAJDI_PACIENTA, parameters = listOf(firstName, lastName, personalNumber))
        val response =  isinClient.get<HttpResponse>(url)
        val json = response.receive<JsonNode>()

        return IsinGetPatientByParametersResultDto(
            result = json.get("vysledek").textValue(),
            resultMessage = json.get("vysledekZprava")?.textValue(),
            patientId = json.get("pacient")?.get("id")?.textValue()
        )
    }

    private fun createIsinURL(
        requestUrl: String,
        baseUrl: String = configuration.rootUrl,
        parameters: List<Any> = listOf(),
        includeIdentification: Boolean = true
    ): String {
        val parametersUrl = parameters.joinToString(separator = "/") { it.toString() }
        return "$baseUrl/$requestUrl/$parametersUrl${if (includeIdentification) userIdentification else ""}"
    }
}
