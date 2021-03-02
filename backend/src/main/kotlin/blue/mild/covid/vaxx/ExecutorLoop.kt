package blue.mild.covid.vaxx

import mu.KLogging
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ExecutorLoop {

    private companion object : KLogging()

    private val executor by lazy { Executors.newScheduledThreadPool(1) }

    fun scheduleRunnableForMinutes(delayInMinutes: Int, taskName: String = "default", scheduledTask: () -> Unit) {
        logger.debug { "Adding task \"$taskName\" to execution pool with delay $delayInMinutes minutes." }
        executor.scheduleWithFixedDelay({
            logger.debug { "Executing scheduled \"$taskName\" task." }
            runCatching(scheduledTask)
                .onFailure { logger.error(it) { "Task \"$taskName\" failed with exception" } }
                .onSuccess { logger.debug { "Task \"$taskName\" executed successfully." } }
        }, 0, delayInMinutes.toLong(), TimeUnit.MINUTES)
    }
}
