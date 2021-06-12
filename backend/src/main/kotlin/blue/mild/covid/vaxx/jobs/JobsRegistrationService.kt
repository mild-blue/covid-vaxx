package blue.mild.covid.vaxx.jobs

import mu.KLogging

/**
 * Service that allows to register all jobs in the executor loop.
 */
class JobsRegistrationService(
    private val periodicExecutorLoop: PeriodicExecutorLoop,
    private val jobsToRun: List<PeriodicJob>
) {

    private companion object : KLogging()

    private var registered = false

    /**
     * Register all jobs for the execution by calling [PeriodicExecutorLoop.schedule].
     */
    fun registerAllJobs() {
        if (registered) {
            throw IllegalStateException("The jobs were already registered! It is not possible to register the same jobs twice.")
        }

        logger.info { "Registering jobs: [${jobsToRun.joinToString(", ") { it.name }}]." }
        jobsToRun.forEach(periodicExecutorLoop::schedule)
        logger.info { "Jobs registered." }
        registered = true
    }
}
