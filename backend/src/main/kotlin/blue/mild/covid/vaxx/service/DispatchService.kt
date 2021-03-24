package blue.mild.covid.vaxx.service

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.launch
import mu.KLogging
import java.util.concurrent.Executors

/**
 * Simple service that is designed as a work dispatcher for asynchronous tasks
 * where we don't need to know the result of the task.
 *
 * Parameter [nThreads] determines how many threads should be allocated for this service.
 */
abstract class DispatchService<T>(private val nThreads: Int) {

    private companion object : KLogging()

    private val channel = Channel<T>(UNLIMITED)

    /**
     * Initialize thread pool.
     */
    protected fun initialize() {
        // create a dispatcher that is used solely for this dispatch service
        val dispatcher = Executors.newFixedThreadPool(nThreads).asCoroutineDispatcher()
        // create a single coroutine, that will handle all emails
        GlobalScope.launch(dispatcher) {
            while (true) {
                val work = channel.receive()
                dispatch(work)
            }
        }
    }

    /**
     * Insert work to channel for immediate dispatch.
     */
    suspend fun insertToChannel(work: T) {
        channel.send(work)
    }

    /**
     * This method is executed when new work arrives to the channel.
     */
    protected abstract suspend fun dispatch(work: T)
}
