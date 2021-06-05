package blue.mild.covid.vaxx.extensions

import mu.KLogging

/**
 * Creates logger with given name.
 */
fun createLogger(name: String) = KLogging().logger("blue.mild.covid.vaxx.$name")

// TODO move this to Katlib
inline fun <T : Any, V : Any> T.applyIfNotNull(value: V?, block: T.(V) -> Unit): T {
    if (value != null) block(value)
    return this
}
