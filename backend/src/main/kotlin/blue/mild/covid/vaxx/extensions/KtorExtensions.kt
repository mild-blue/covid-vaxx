package blue.mild.covid.vaxx.extensions

import io.ktor.features.origin
import io.ktor.request.ApplicationRequest
import io.ktor.request.header

/**
 * Determine real IP address of the request call from the proxy headers.
 */
fun ApplicationRequest.determineRealIp() =
    realIpHeader() ?: forwardedForHeader() ?: origin.remoteHost

private fun ApplicationRequest.realIpHeader(): String? =
    header("x-real-ip")

private fun ApplicationRequest.forwardedForHeader(): String? =
    header("x-forwarded-for")
