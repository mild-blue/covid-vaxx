package blue.mild.covid.vaxx.jobs

import java.util.concurrent.TimeUnit

data class PeriodicJob(
    /**
     * Name of the job.
     */
    val name: String,
    /**
     * Delay between the jobs.
     */
    val delay: Long,
    /**
     * Units for [delay].
     */
    val unit: TimeUnit,
    /**
     * Job that will be executed.
     */
    val job: Job
)
