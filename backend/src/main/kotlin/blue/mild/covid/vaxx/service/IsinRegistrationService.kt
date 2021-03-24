package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.config.IsinConfigurationDto
import blue.mild.covid.vaxx.dto.internal.StoreVaccinationRequestDto
import io.ktor.client.HttpClient
import mu.KLogging

/**
 * Registering patients in ISIN.
 */
@Suppress("unused")
class IsinRegistrationService(
    private val configuration: IsinConfigurationDto,
    private val httpClient: HttpClient,
    private val patientService: PatientService,
    private val vaccinationService: VaccinationService,
    nThreads: Int = 1
) : MedicalRegistrationService, DispatchService<StoreVaccinationRequestDto>(nThreads) {

    private companion object : KLogging()

    /**
     * Asynchronously registers patient's vaccination ISIN.
     */
    override suspend fun registerPatientsVaccination(patientVaccination: StoreVaccinationRequestDto) {
        insertToChannel(patientVaccination)
    }

    override suspend fun dispatch(work: StoreVaccinationRequestDto) {
        // do not forget to store that the export was successful
        // vaccinationService.exportedToIsin(vaccinationId = work.vaccinationId, storedOn = Instant.now())
        TODO("Not yet implemented")
    }
}
