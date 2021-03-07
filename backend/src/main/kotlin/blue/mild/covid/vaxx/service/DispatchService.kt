package blue.mild.covid.vaxx.service

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

abstract class DispatchService<T>(private val nThreads: Int) {

    private val channel = Channel<T>(UNLIMITED)

    protected fun initialize() {
        // create a dispatcher that is used solely for sending the emails
        val dispatcher = Executors.newFixedThreadPool(nThreads).asCoroutineDispatcher()
        // create a single coroutine, that will handle all emails
        GlobalScope.launch(dispatcher) {
            while (true) {
                val work = channel.receive()
                dispatch(work)
            }
        }
    }

    suspend fun insertToChannel(work: T) {
        channel.send(work)
    }

    protected abstract suspend fun dispatch(work: T)
}
