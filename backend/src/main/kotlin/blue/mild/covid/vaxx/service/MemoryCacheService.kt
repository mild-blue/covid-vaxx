package blue.mild.covid.vaxx.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MemoryCacheService<T : Any>(
    private val dataInit: suspend () -> Collection<T>,
    private val dataRefresh: suspend () -> Collection<T> = dataInit
) {
    private lateinit var cache: Collection<T>
    private val cacheLock = Mutex()

    /**
     * Initialize data.
     */
    suspend fun initialize() {
        cacheLock.withLock { cache = dataInit() }
    }

    /**
     * Executes refresh function from the constructor.
     */
    suspend fun refresh(): List<T> = cacheLock.withLock {
        cache = dataRefresh()
        cache.toList()
    }

    /**
     * Refreshes the cache with given data.
     */
    suspend fun refresh(data: Collection<T>) {
        cacheLock.withLock {
            cache = data.toList()
        }
    }

    /**
     * Returns data in copied list.
     */
    suspend fun value(): List<T> = cacheLock.withLock { cache.toList() }
}
