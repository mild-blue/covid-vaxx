package blue.mild.covid.vaxx.jobs

import mu.KLogging
import org.slf4j.MarkerFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Thread pool that allows to schedule a periodic job.
 */
class PeriodicExecutorLoop(
    private val corePoolSize: Int = 1,
    private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(corePoolSize)
) : ExecutorService by executor {

    private companion object : KLogging()

    /**
     * Schedule given [job] in the [executor].
     */
    fun schedule(
        job: PeriodicJob
    ) = scheduleSafe(job.delay, job.unit, job.name, job.job)


    private fun scheduleSafe(delay: Long, unit: TimeUnit, jobName: String, job: Job) {
        val marker = MarkerFactory.getMarker("executor:$jobName")
        logger.info(marker) { "Adding job \"$jobName\" to execution pool with delay $delay $unit." }
        executor.scheduleWithFixedDelay({
            logger.info(marker) { "Executing scheduled \"$jobName\" job." }
            runCatching { job.execute() }
                .onFailure { logger.error(marker, it) { "Job \"$jobName\" failed with an exception." } }
                .onSuccess { logger.info(marker) { "Job \"$jobName\" executed successfully." } }
        }, 0, delay, unit)
    }
}

