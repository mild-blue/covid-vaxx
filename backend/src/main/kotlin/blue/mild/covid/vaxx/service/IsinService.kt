package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.VaccinationBodyPart
import blue.mild.covid.vaxx.dto.config.IsinConfigurationDto
import blue.mild.covid.vaxx.dto.internal.IsinGetPatientByParametersResultDto
import blue.mild.covid.vaxx.dto.internal.IsinPostPatientContactInfoDto
import blue.mild.covid.vaxx.dto.internal.IsinPostPatientContactInfoDtoIn
import blue.mild.covid.vaxx.dto.internal.IsinVaccinationCreateOrUpdateDtoIn
import blue.mild.covid.vaxx.dto.internal.IsinVaccinationDoseCreateOrUpdateDtoIn
import blue.mild.covid.vaxx.dto.internal.IsinVaccinationDoseDto
import blue.mild.covid.vaxx.dto.internal.IsinVaccinationDto
import blue.mild.covid.vaxx.dto.internal.StoreVaccinationRequestDto
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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale


class IsinService(
    private val configuration: IsinConfigurationDto,
    private val isinClient: HttpClient
) : IsinServiceInterface {

    private val userIdentification =
        "?pcz=${encodeValue(configuration.pracovnik.pcz)}&pracovnikNrzpCislo=${encodeValue(configuration.pracovnik.nrzpCislo)}"

    private companion object : KLogging() {
        const val URL_GET_PATIENT_BY_PARAMETERS = "pacienti/VyhledatDleJmenoPrijmeniRc"
        const val URL_GET_FOREIGNER_BY_INSURANCE_NUMBER = "pacienti/VyhledatCizinceDleCislaPojistence"
        const val URL_GET_VACCINATIONS_BY_PATIENT_ID = "vakcinace/NacistVakcinacePacienta"
        const val URL_UPDATE_PATIENT_INFO = "pacienti/AktualizujKontaktniUdajePacienta"
        const val URL_CREATE_OR_CHANGE_VACCINATION = "vakcinace/VytvorNeboZmenVakcinaci"
        const val URL_CREATE_OR_CHANGE_DOSE = "vakcinace/VytvorNeboZmenDavku"
        const val URL_UPDATE_VACCINATION_STATE = "vakcinace/ZmenStavVakcinace" // This is used to clean the test data
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
        logger.info { "Executing ISIN HTTP call ${URL_GET_PATIENT_BY_PARAMETERS}." }
        val json =  isinClient.get<JsonNode>(url)

        val result = IsinGetPatientByParametersResultDto(
            result = json.get("vysledek").textValue(),
            resultMessage = json.get("vysledekZprava")?.textValue(),
            patientId = json.get("pacient")?.get("id")?.textValue()
        )
        logger.info {
            "Data from ISIN for patient ${firstName} ${lastName}, personalNumber=${personalNumber}: " +
            "result=${result.result}, resultMessage=${result.resultMessage}, patientId=${result.patientId}."
        }
        return result
    }

    override suspend fun getForeignerByInsuranceNumber(
        insuranceNumber: String
    ): IsinGetPatientByParametersResultDto {
        val url = createIsinURL(URL_GET_FOREIGNER_BY_INSURANCE_NUMBER, parameters = listOf(
            insuranceNumber.trim()
        ))
        logger.info { "Executing ISIN HTTP call ${URL_GET_FOREIGNER_BY_INSURANCE_NUMBER}." }
        val json =  isinClient.get<JsonNode>(url)

        val result = IsinGetPatientByParametersResultDto(
            result = json.get("vysledek").textValue(),
            resultMessage = json.get("vysledekZprava")?.textValue(),
            patientId = json.get("pacient")?.get("id")?.textValue()
        )
        logger.info {
            "Data from ISIN for foreigner insuranceNumber=${insuranceNumber}: " +
            "result=${result.result}, resultMessage=${result.resultMessage}, patientId=${result.patientId}."
        }
        return result
    }

    private suspend fun getPatientVaccinations(isinId: String): List<IsinVaccinationDto> {
        val url = createIsinURL(URL_GET_VACCINATIONS_BY_PATIENT_ID, parameters = listOf(
            isinId.trim()
        ))
        logger.info { "Executing ISIN HTTP call ${URL_GET_VACCINATIONS_BY_PATIENT_ID}." }
        return isinClient.get<HttpResponse>(url) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }.receive()
    }

    override suspend fun tryPatientIsReadyForVaccination(isinId: String): Boolean? =
        runCatching {
            val allVaccinations = getPatientVaccinations(isinId)
            logger.info(
                "Getting vaccination from ISIN for patient ${isinId } was successful. " +
                "${allVaccinations.count()} vaccinations were found."
            )

            val problematicVaccinations = allVaccinations.filter {
                    vaccination -> vaccination.typOckovaniKod == "CO19" && vaccination.stav != "Zruseno"
            }

            if (problematicVaccinations.count() > 0) {
                logger.info(
                    "${problematicVaccinations.count()} problematic vaccinations of patient ${isinId} were found in ISIN. " +
                    "Patient is not ready for vaccination: ${problematicVaccinations}"
                )
                false
            } else {
                logger.info(
                    "No problematic vaccination of patient ${isinId} were found in ISIN. " +
                    "Patient is ready for vaccination."
                )
                true
            }
        }.getOrElse {
            logger.warn { "Data retrieval from ISIN - failure." }
            val wrappingException =
                Exception("An exception ${it.javaClass.canonicalName} was thrown! - ${it.message}\n${it.stackTraceToString()}")
            logger.error(wrappingException) {
                "Getting vaccinations from ISIN failed for patient with ISIN ID ${isinId}."
            }
            null
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
                logger.warn { "Data retrieval from ISIN - failure." }
                val wrappingException =
                    Exception("An exception ${it.javaClass.canonicalName} was thrown! - ${it.message}\n${it.stackTraceToString()}")
                logger.error(wrappingException) {
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

        logger.info { "Executing ISIN HTTP call ${URL_UPDATE_PATIENT_INFO}." }
        return isinClient.post<HttpResponse>(url) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            body = data
        }.receive()
    }

    /**
     * If vaccination dose number is 1, creates new vaccination and creates 1st dose in in.
     * If vaccination dose number is 2, gets the ongoing vaccination and creates 2nd dose in it.
     */
    @Suppress("ReturnCount", "LongMethod")
    override suspend fun tryCreateVaccination(
        vaccination: StoreVaccinationRequestDto,
        patient: PatientDtoOut
    ): Boolean {
        if (vaccination.doseNumber != 1 && vaccination.doseNumber != 2) {
            throw IllegalArgumentException("Dose number was ${vaccination.doseNumber} which is not valid.")
        }

        if (patient.isinId == null) {
            logger.info("No ISIN ID provided for patient ${patient.id}. Skipping vaccination creating in ISIN.")
            return false
        }
        if (vaccination.vaccineExpiration == null) {
            logger.info("No vaccine expiration provided for vaccination ${vaccination.vaccinationId}. Skipping vaccination creating in ISIN.")
            return false
        }
        if (vaccination.doseNumber == 1 && patient.isinReady != true) {
            logger.info("Patient ${patient.id} is not ISIN ready. Skipping vaccination creating in ISIN.")
            return false
        }

        val vaccinationExpirationInstant =
            vaccination.vaccineExpiration.atTime(LocalTime.MIDNIGHT).atZone(ZoneId.systemDefault()).toInstant()

        val defaultIndication = "J01"
        val indication = if (patient.indication == null || patient.indication.isBlank())
            defaultIndication
        else
            patient.indication

        return runCatching {
            val isinVaccination: IsinVaccinationDto? = if (vaccination.doseNumber == 1) {
                createVaccination(
                    IsinVaccinationCreateOrUpdateDtoIn(
                        id = null,
                        pacientId = patient.isinId,
                        typOckovaniKod = "CO19",
                        indikace = listOf(indication),
                        indikaceJina = if (indication == defaultIndication) configuration.indikaceJina else null
                    )
                )
            } else {
                tryPatientIsReadyForSecondDose(patient.isinId)
            }

            if (isinVaccination == null) {
                logger.info(
                    "No vaccination with first dose was found in ISIN for patient ${patient.id}. " +
                            "Skipping vaccination creating in ISIN."
                )
                return false
            }

            if (isinVaccination.id == null) {
                throw NoSuchFieldException("We expect ISIN return non null vaccination id.")
            }

            logger.debug("Creating or getting vaccination in ISIN was successful for patient with ISIN ID ${patient.isinId}.")
            logger.debug("Data obtained from ISIN: $isinVaccination")

            val dose = createVaccinationDose(
                IsinVaccinationDoseCreateOrUpdateDtoIn(
                    id = null,
                    vakcinaceId = isinVaccination.id,
                    ockovaciLatkaKod = configuration.ockovaciLatkaKod,
                    datumVakcinace = vaccination.vaccinatedOn,
                    typVykonuKod = vaccination.doseNumber.toString(),
                    sarze = vaccination.vaccineSerialNumber,
                    aplikacniCestaKod = "IM",
                    mistoAplikaceKod = if (vaccination.bodyPart == VaccinationBodyPart.DOMINANT_HAND) "DP" else "NP",
                    expirace = vaccinationExpirationInstant,
                    poznamka = vaccination.notes,
                    stav = null
                )
            )

            logger.debug("Creating dose ${vaccination.doseNumber} in ISIN was successful for patient with ISIN ID ${patient.isinId}.")
            logger.debug("Data obtained from ISIN: $dose")

            logger.info("Exporting vaccination with dose ${vaccination.doseNumber} to ISIN was successful for patient with ISIN ID ${patient.isinId}.")
            true
        }.getOrElse {
            logger.warn { "Data retrieval from ISIN - failure." }
            val wrappingException =
                Exception("An exception ${it.javaClass.canonicalName} was thrown! - ${it.message}\n${it.stackTraceToString()}")
            logger.error(wrappingException) {
                "Exporting vaccination to ISIN failed for patient with ISIN ID ${patient.isinId}"
            }
            false
        }
    }

    suspend fun tryPatientIsReadyForSecondDose(isinId: String): IsinVaccinationDto? =
        runCatching {
            val allVaccinations = getPatientVaccinations(isinId)
            logger.info(
                "Getting vaccination from ISIN for patient ${isinId} was successful. " +
                "${allVaccinations.count()} vaccinations were found."
            )

            val ongoingVaccinations = allVaccinations.filter {
                    vaccination -> vaccination.typOckovaniKod == "CO19" && vaccination.stav == "Probihajici"
            }

            if (ongoingVaccinations.count() != 1) {
                logger.info(
                    "1 ongoing covid vaccination is expected. ${ongoingVaccinations.count()} " +
                            "ongoing vaccinations were found in ISIN for ISIN id ${isinId}. " +
                            "Patient is not ready for 2nd dose"
                )
                null
            } else {
                logger.info(
                    "1 ongoing covid vaccination was found in ISIN for ISIN id ${isinId}. " +
                            "Patient is ready for 2nd dose."
                )
                ongoingVaccinations[0]
            }
        }.getOrElse {
            logger.warn { "Data retrieval from ISIN - failure." }
            val wrappingException =
                Exception("An exception ${it.javaClass.canonicalName} was thrown! - ${it.message}\n${it.stackTraceToString()}")
            logger.error(wrappingException) {
                "Getting vaccinations from ISIN failed for patient with ISIN ID ${isinId}."
            }
            null
        }

    // TODO share logic with createVaccinationDose function
    private suspend fun createVaccination(vaccinationDtoIn: IsinVaccinationCreateOrUpdateDtoIn): IsinVaccinationDto {
        val url = createIsinURL(URL_CREATE_OR_CHANGE_VACCINATION)
        val data = if (vaccinationDtoIn.pracovnik == null)
            vaccinationDtoIn.copy(pracovnik = configuration.pracovnik)
        else
            vaccinationDtoIn

        logger.info { "Executing ISIN HTTP call ${URL_CREATE_OR_CHANGE_VACCINATION}." }
        return isinClient.post<HttpResponse>(url) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            body = data
        }.receive()
    }

    // TODO share logic with createVaccination function
    private suspend fun createVaccinationDose(vaccinationDoseDtoIn: IsinVaccinationDoseCreateOrUpdateDtoIn): IsinVaccinationDoseDto {
        val url = createIsinURL(URL_CREATE_OR_CHANGE_DOSE)
        val data = if (vaccinationDoseDtoIn.pracovnik == null)
            vaccinationDoseDtoIn.copy(pracovnik = configuration.pracovnik)
        else
            vaccinationDoseDtoIn

        logger.info { "Executing ISIN HTTP call ${URL_CREATE_OR_CHANGE_DOSE}." }
        return isinClient.post<HttpResponse>(url) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            body = data
        }.receive()
    }

    // This is used to clean the test data and should never be used in production
    override suspend fun cancelAllVaccinations(isinId: String) {
        val allVaccinations = getPatientVaccinations(isinId)
        var canceled = 0

        allVaccinations.filter { vaccination ->
            vaccination.typOckovaniKod == "CO19" &&
                    vaccination.stav != "Zruseno"
        }.forEach { vaccination ->
            if(vaccination.id != null) {
                cancelVaccination(vaccination.id)
                canceled++
            }
        }

        logger.info { "${canceled} vaccinations canceled successfully for patient with ISIN id ${isinId}" }
    }

    private suspend fun cancelVaccination(vaccinationId: String): HttpResponse {
        val url = createIsinURL(URL_UPDATE_VACCINATION_STATE, parameters = listOf(
            vaccinationId,
            "Zruseno"
        ))
        val data = mapOf("pracovnik" to configuration.pracovnik)

        logger.info { "Executing ISIN HTTP call ${URL_UPDATE_VACCINATION_STATE}." }
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
        val parametersUrl = parameters.map{encodeValue(it)}.joinToString(separator = "/")
        val url = "$baseUrl/$requestUrl/$parametersUrl${if (includeIdentification) userIdentification else ""}"
        if (!url.isUrl()) {
            // we want to print that to the log as well as we're facing a stack overflow somewhere here
            logger.warn { "Created ISIN URL for patient is not valid URL! - $url." }
            throw IllegalStateException("Created ISIN URL for patient is not valid URL! - $url.")
        }
        return url
    }

    private fun encodeValue(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replace("+", "%20")
    }
}
