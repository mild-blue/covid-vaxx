package blue.mild.covid.vaxx.jobs

import blue.mild.covid.vaxx.dto.request.IsinJobDtoIn
import blue.mild.covid.vaxx.service.IsinRetryService
import kotlinx.coroutines.runBlocking
import mu.KLogging
import mu.Marker
import org.slf4j.MarkerFactory

/**
 * Job that verifies that database is in sync with ISIN.
 */
class IsinSyncJob(private val isinRetryService: IsinRetryService) : Job {

    private companion object : KLogging() {
        val marker: Marker = MarkerFactory.getMarker("job:${IsinSyncJob::class.simpleName}")
    }

    override fun execute(): Unit = runBlocking {
        logger.debug(marker) { "Starting ISIN retry service." }
        isinRetryService.runIsinRetry(IsinJobDtoIn(
            exportPatientsInfo = true,
            checkVaccinations = true,
            exportVaccinationsFirstDose = true,
            exportVaccinationsSecondDose = true,
            validatePatients = true,
            patientsCount = null, //check all patients in the database
            patientsOffset = 0
        ))
        
        logger.info(marker) { "ISIN retry service finished." }
    }
}
