package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.config.IsinConfigurationDto
import blue.mild.covid.vaxx.dto.internal.IsinGetPatientByParametersResultDto
import blue.mild.covid.vaxx.dto.internal.IsinPostPatientContactInfoDto
import blue.mild.covid.vaxx.dto.internal.IsinPostPatientContactInfoDtoIn
import blue.mild.covid.vaxx.dto.response.PatientDtoOut
import blue.mild.covid.vaxx.utils.normalizePersonalNumber
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import mu.KLogging
import java.net.URL
import java.util.Locale


class IsinService(
    private val configuration: IsinConfigurationDto,
    private val isinClient: HttpClient
) : IsinServiceInterface {

    private val userIdentification =
        "?pcz=${configuration.pracovnik.pcz}&pracovnikNrzpCislo=${configuration.pracovnik.nrzpCislo}"

    private companion object : KLogging() {
        const val URL_GET_PATIENT_BY_PARAMETERS = "pacienti/VyhledatDleJmenoPrijmeniRc";
        const val URL_UPDATE_PATIENT_INFO = "pacienti/AktualizujKontaktniUdajePacienta";
    }

    override suspend fun getPatientByParameters(
        firstName: String,
        lastName: String,
        personalNumber: String
    ): IsinGetPatientByParametersResultDto {
        val url = createIsinURL(URL_GET_PATIENT_BY_PARAMETERS, parameters = listOf(
            firstName.trim().uppercase(Locale.getDefault()),
            lastName.trim().uppercase(Locale.getDefault()),
            personalNumber.normalizePersonalNumber()
        ))
        logger.info { "Executing ISIN HTTP call." }
        val response =  isinClient.get<HttpResponse>(url)
        val json = response.receive<JsonNode>()

        return IsinGetPatientByParametersResultDto(
            result = json.get("vysledek").textValue(),
            resultMessage = json.get("vysledekZprava")?.textValue(),
            patientId = json.get("pacient")?.get("id")?.textValue()
        )
    }

    override suspend fun tryExportPatientContactInfo(patient: PatientDtoOut, notes: String?): Boolean {
        return if (patient.isinId != null) {
            runCatching {
                val contactInfoOut = exportPatientContactInfo(
                    IsinPostPatientContactInfoDtoIn(
                        zdravotniPojistovnaKod = patient.insuranceCompany.code.toString(),
                        kontaktniMobilniTelefon = patient.phoneNumber,
                        kontaktniEmail = patient.email,

                        // This ISIN api call is successful only if pobytMesto and pobytPsc matches which does not
                        // need to be the case. This is why we do not update these values.
                        pobytMesto = null, // patient.district,
                        pobytPsc = null, // patient.zipCode.toString(),

                        notifikovatEmail = true,
                        notifikovatSms = true,
                        poznamka = notes,
                        id = patient.isinId
                    )
                )
                logger.info("Exporting patient information to ISIN was successful for patient with ISIN ID ${patient.isinId}.")
                logger.debug("Data obtained from ISIN: $contactInfoOut")
                true
            }.getOrElse {
                logger.error(it) {
                    "Exporting patient information to ISIN failed for patient with ISIN ID ${patient.isinId}"
                }
                false
            }
        } else {
            logger.info("No ISIN ID provided for patient ${patient.id}. Skipping exporting patient information to ISIN.")
            false
        }
    }

    private suspend fun exportPatientContactInfo(contactInfo: IsinPostPatientContactInfoDtoIn): IsinPostPatientContactInfoDto {
        val url = createIsinURL(URL_UPDATE_PATIENT_INFO)
        val data = if (contactInfo.pracovnik == null)
            contactInfo.copy(pracovnik = configuration.pracovnik)
        else
            contactInfo

        logger.info { "Executing ISIN HTTP call." }
        return isinClient.post<HttpResponse>(url) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            body = data
        }.receive()
    }

    private fun String.isUrl() = runCatching {
        URL(this).toURI()
    }.isSuccess

    private fun createIsinURL(
        requestUrl: String,
        baseUrl: String = configuration.rootUrl,
        parameters: List<String> = listOf(),
        includeIdentification: Boolean = true
    ): String {
        val parametersUrl = parameters.joinToString(separator = "/")
        val url = "$baseUrl/$requestUrl/$parametersUrl${if (includeIdentification) userIdentification else ""}"
        if (!url.isUrl()) {
            // we want to print that to the log as well as we're facing a stack overflow somewhere here
            logger.warn { "Created ISIN URL for patient is not valid URL! - $url." }
            throw IllegalStateException("Created ISIN URL for patient is not valid URL! - $url.")
        }
        return url
    }
}
