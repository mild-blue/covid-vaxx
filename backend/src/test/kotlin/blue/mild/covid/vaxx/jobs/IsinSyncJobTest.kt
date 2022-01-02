package blue.mild.covid.vaxx.jobs

import blue.mild.covid.vaxx.dto.request.IsinJobDtoIn
import blue.mild.covid.vaxx.service.IsinRetryService
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Test

class IsinSyncJobTest {

    @Test
    fun `job should initiate synchronization of database with ISIN`() {

        // job should initiate IsinRetryService for all checks and exports of every
        // single patient in the database
        val isinRetryServiceMock = mockk<IsinRetryService>(relaxed = true)

        val job = IsinSyncJob(isinRetryServiceMock)
        job.execute()
        coVerify { isinRetryServiceMock.runIsinRetry(
            IsinJobDtoIn(
            exportPatientsInfo = true,
            checkVaccinations = true,
            exportVaccinationsFirstDose = true,
            exportVaccinationsSecondDose = true,
            validatePatients = true,
            patientsCount = null,
            patientsOffset = 0)
        ) }
    }
}
