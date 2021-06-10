package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.config.IsinConfigurationDto
import blue.mild.covid.vaxx.dto.internal.IsinValidationResultDto
import blue.mild.covid.vaxx.dto.internal.PatientValidationResult
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.utils.normalizePersonalNumber
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import mu.KLogging
import java.net.URL


class IsinValidationService(
    private val configuration: IsinConfigurationDto,
    private val isinClient: HttpClient
) : PatientValidationService {

    private val userIdentification =
        "?pcz=${configuration.pracovnik.pcz}&pracovnikNrzpCislo=${configuration.pracovnik.nrzpCislo}"

    private companion object : KLogging() {
        const val URL_NAJDI_PACIENTA = "pacienti/VyhledatDleJmenoPrijmeniRc"
    }

    private enum class VyhledaniPacientaResult {
        PacientNalezen,
        NalezenoVicePacientu,
        PacientNebylNalezen,
        CizinecZaloz,
        ChybaVstupnichDat,
        Chyba
    }

    override suspend fun validatePatient(registrationDto: PatientRegistrationDtoIn): IsinValidationResultDto {
        val firstName = registrationDto.firstName
        val lastName = registrationDto.lastName
        val personalNumber = registrationDto.personalNumber
            ?: throw IllegalArgumentException("Personal number cannot be null for ISIN validation")

        val json = runCatching {
            getPatientResponse(
                firstName = firstName,
                lastName = lastName,
                personalNumber = personalNumber
            )
        }.onSuccess {
            logger.info { "Data retrieval from ISIN - success." }
        }.onFailure {
            logger.warn { "Data retrieval from ISIN - failure." }
            // TODO #287 we think that this is the place which produces stack overflow
            val wrappingException =
                Exception("An exception ${it.javaClass.canonicalName} was thrown! - ${it.message}\n${it.stackTraceToString()}")
            logger.error(wrappingException) {
                "Getting data from ISIN server failed for patient ${firstName}/${lastName}/${personalNumber}"
            }
        }.getOrNull() ?: return IsinValidationResultDto(status = PatientValidationResult.WAS_NOT_VERIFIED)

        val result = json.get("vysledek")?.textValue()
        val resultMessage = json.get("vysledekZprava")?.textValue()
        val patientId = json.get("pacient")?.get("id")?.textValue()

        logger.info {
            "Data from ISIN for patient ${firstName}/${lastName}/${personalNumber}: " +
                    "result=${result}, resultMessage=${resultMessage}, patientId=${patientId}."
        }

        return when (result) {
            VyhledaniPacientaResult.PacientNalezen.name,
            VyhledaniPacientaResult.NalezenoVicePacientu.name ->
                IsinValidationResultDto(
                    status = PatientValidationResult.PATIENT_FOUND,
                    patientId = patientId
                )
            VyhledaniPacientaResult.PacientNebylNalezen.name,
            VyhledaniPacientaResult.ChybaVstupnichDat.name ->
                IsinValidationResultDto(
                    status = PatientValidationResult.PATIENT_NOT_FOUND
                )
            else ->
                IsinValidationResultDto(
                    status = PatientValidationResult.WAS_NOT_VERIFIED
                )
        }
    }

    private suspend fun getPatientResponse(firstName: String, lastName: String, personalNumber: String): JsonNode {
        val url = createIsinURL(
            URL_NAJDI_PACIENTA,
            parameters = listOf(
                firstName.trim().uppercase(),
                lastName.trim().uppercase(),
                personalNumber.normalizePersonalNumber()
            )
        )

        if (!url.isUrl()) {
            // we want to print that to the log as well as we're facing a stack overflow somewhere here
            logger.warn { "Created ISIN URL for patient is not valid URL! - $url." }
            throw IllegalStateException("Created ISIN URL for patient is not valid URL! - $url.")
        }
        logger.info { "Executing ISIN HTTP call." }
        return isinClient.get(url)
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
        return "$baseUrl/$requestUrl/$parametersUrl${if (includeIdentification) userIdentification else ""}"
    }
}
