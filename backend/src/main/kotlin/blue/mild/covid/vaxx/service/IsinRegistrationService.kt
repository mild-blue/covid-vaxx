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
    private val patientService: PatientService,
    nThreads: Int = 1
) : MedicalRegistrationService, DispatchService<UUID>(nThreads) {

    private companion object : KLogging()

    /**
     * Asynchronously registers patient's vaccination ISIN.
     */
    override suspend fun registerPatientsVaccination(patientId: UUID) {
        insertToChannel(patientId)
    }

    override suspend fun dispatch(work: UUID) {
        TODO("Not yet implemented")
    }
}
