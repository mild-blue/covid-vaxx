package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.config.IsinConfigurationDto
import blue.mild.covid.vaxx.dto.internal.IsinValidationResultDto
import blue.mild.covid.vaxx.dto.internal.IsinValidationResultStatus
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.utils.normalizePersonalNumber
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import mu.KLogging
import org.apache.http.ssl.SSLContextBuilder
import java.io.File
import java.security.KeyStore
import java.util.Locale

private const val URL_NAJDI_PACIENTA = "pacienti/VyhledatDleJmenoPrijmeniRc"

class IsinValidationService(
    private val configuration: IsinConfigurationDto
) {
    private val isinClient = client(configuration)

    private val userIdentification = "?pcz=${configuration.pracovnik.pcz}&pracovnikNrzpCislo=${configuration.pracovnik.nrzpCislo}"

    private companion object : KLogging()

    private enum class VyhledaniPacientaResult {
        PacientNalezen,
        NalezenoVicePacientu,
        PacientNebylNalezen,
        CizinecZaloz,
        ChybaVstupnichDat,
        Chyba
    }

    suspend fun validatePatientIsin(registrationDto: PatientRegistrationDtoIn): IsinValidationResultDto {
        val response = runCatching {
            getPatientResponse(
                jmeno = registrationDto.firstName.trim().uppercase(Locale.getDefault()),
                prijmeni = registrationDto.lastName.trim().uppercase(Locale.getDefault()),
                rodneCislo = registrationDto.personalNumber.normalizePersonalNumber()
            )
        }.getOrElse {
            logger.error(it) { "Getting data from isin server failed" }
            return IsinValidationResultDto( status = IsinValidationResultStatus.WAS_NOT_VERIFIED )
        }

        val json = response.receive<JsonNode>()

        val result = json.get("vysledek").textValue()

        logger.debug { "Isin result: ${result}, message: ${json.get("vysledekZprava").textValue()}" }

        return when (result) {
            VyhledaniPacientaResult.PacientNalezen.name ->
                IsinValidationResultDto(
                    status = IsinValidationResultStatus.PATIENT_FOUND,
                    patientId = json.get("pacient").get("id").textValue()
                )
            VyhledaniPacientaResult.PacientNebylNalezen.name,
            VyhledaniPacientaResult.ChybaVstupnichDat.name ->
                IsinValidationResultDto(
                    status = IsinValidationResultStatus.PATIENT_NOT_FOUND
                )
            else ->
                IsinValidationResultDto(
                    status = IsinValidationResultStatus.WAS_NOT_VERIFIED
                )
        }
    }

    private suspend fun getPatientResponse(jmeno: String, prijmeni: String, rodneCislo: String): HttpResponse {
        val url = createIsinURL(URL_NAJDI_PACIENTA, parameters = listOf(jmeno, prijmeni, rodneCislo))
        return isinClient.get<HttpResponse>(url)
    }

    private fun createIsinURL(requestUrl: String, baseUrl: String = configuration.rootUrl, parameters: List<Any> = listOf(), includeIdentification: Boolean = true): String {
        val parametersUrl = parameters.map { it.toString() }.joinToString(separator = "/")
        return "$baseUrl/$requestUrl/$parametersUrl${if (includeIdentification) userIdentification else ""}"
    }

    private fun client(
        config: IsinConfigurationDto
    ) =
        HttpClient(Apache) {
            install(JsonFeature) {
                serializer = JacksonSerializer()
            }

            configureCertificates(config)
        }

    private fun HttpClientConfig<ApacheEngineConfig>.configureCertificates(config: IsinConfigurationDto) {
        engine {
            customizeClient {
                setSSLContext(
                    SSLContextBuilder
                        .create()
                        .loadKeyMaterial(readStore(config), config.keyPass.toCharArray())
                        .build()
                )
            }
        }
    }

    private fun readStore(config: IsinConfigurationDto): KeyStore? =
        runCatching {
            File(config.storePath).inputStream().use {
                KeyStore.getInstance(config.storeType).apply {
                    load(it, config.storePass.toCharArray())
                }
            }
        }.onFailure {
            logger.error(it) { "It was not possible to load key store!" }
        }.onSuccess {
            logger.debug { "KeyStore loaded." }
        }.getOrNull()
}
