package blue.mild.covid.vaxx.monitoring

/**
 * MDC variable name for the call ID.
 */
const val CALL_ID = "callId"

/**
 * Remote host - IP address of the caller.
 */
const val REMOTE_HOST = "remoteHost"

/**
 * Ktor path in the request.
 */
const val PATH = "path"

/**
 * See https://docs.aws.amazon.com/elasticloadbalancing/latest/application/load-balancer-request-tracing.html.
 */
const val AMAZON_TRACE = "amazonTrace"
