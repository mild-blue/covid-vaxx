package blue.mild.covid.vaxx.jobs

import blue.mild.covid.vaxx.extensions.createLogger
import blue.mild.covid.vaxx.setup.EnvVariables
import dev.forst.katlib.getEnv
import dev.forst.katlib.whenNull
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import java.util.concurrent.TimeUnit

private val logger = createLogger("JobRegistration")

/**
 * DI providing instances of all jobs and necessary infrastructure.
 *
 * By default are all jobs disabled - in order to enable them, set correct [EnvVariables].
 */
fun DI.MainBuilder.registerPeriodicJobs() {
    // register jobs
    bind<EmailRetryJob>() with singleton { EmailRetryJob(instance(), instance()) }
    bind<IsinSyncJob>() with singleton { IsinSyncJob(instance()) }

    // register infrastructure
    bind<PeriodicExecutorLoop>() with singleton {
        PeriodicExecutorLoop(corePoolSize = 1)
    }

    bind<JobsRegistrationService>() with singleton {
        JobsRegistrationService(
            instance(),
            jobsToRun = listOfNotNull(
                registerIfEnvEnabled(EnvVariables.ENABLE_PERIODIC_EMAIL_RETRY) { repeatedEmailJob(instance()) },
                registerIfEnvEnabled(EnvVariables.ENABLE_PERIODIC_ISIN_SYNC) { repeatedIsinSyncJob(instance()) }
            )
        )
    }
}

// register the job only if the env variable is true
private fun registerIfEnvEnabled(variable: EnvVariables, jobFactory: () -> PeriodicJob): PeriodicJob? =
    getEnv(variable.name)
        ?.takeIf { it.toBoolean() }
        .whenNull { logger.warn { "Job for ${variable.name} is disabled." } }
        ?.let { jobFactory() }
        ?.also { logger.info { "Enabling \"${it.name}\" job." } }

private fun repeatedEmailJob(job: EmailRetryJob) = PeriodicJob(
    name = requireNotNull(job::class.simpleName) { "Classes do have a name." },
    delay = 1, // once in an hour we try to send the emails
    unit = TimeUnit.HOURS,
    job = job
)

private fun repeatedIsinSyncJob(job: IsinSyncJob) = PeriodicJob(
    name = requireNotNull(job::class.simpleName) { "Classes do have a name." },
    delay = getEnv(EnvVariables.ISIN_SYNC_PERIOD.name)?.toLong() ?: TimeUnit.HOURS.toMillis(1), // once in an sync period (default 1 hour) synchronize with ISIN
    unit = TimeUnit.MILLISECONDS,
    job = job
)
