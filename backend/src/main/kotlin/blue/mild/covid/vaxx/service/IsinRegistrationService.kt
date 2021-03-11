package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.config.IsinConfigurationDto
import io.ktor.client.HttpClient
import mu.KLogging
import java.util.UUID

/**
 * Registering patients in ISIN.
 */
class IsinRegistrationService(
    private val configuration: IsinConfigurationDto,
    private val httpClient: HttpClient,
    private val patientService: PatientService
) : MedicalRegistrationService {
    private companion object : KLogging()

    /**
     * Registers patient's vaccination ISIN.
     */
    override suspend fun registerPatientsVaccination(patientId: UUID) {
        TODO("Not yet implemented")
    }
}
