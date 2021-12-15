package blue.mild.covid.vaxx.extensions

import mu.KLogging

/**
 * Creates logger with given name.
 */
fun createLogger(name: String) = KLogging().logger("blue.mild.covid.vaxx.$name")
