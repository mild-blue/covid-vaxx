package blue.mild.covid.vaxx.dto.internal

import blue.mild.covid.vaxx.security.auth.UserPrincipal

/**
 * Wrapper data class that provides access to payload as well as
 * metadata about current request.
 */
sealed class ContextAware<T> {
    abstract val remoteHost: String
    abstract val callId: String?
    abstract val payload: T

    data class PublicContext<T>(
        override val remoteHost: String,
        override val callId: String?,
        override val payload: T
    ) : ContextAware<T>()

    data class AuthorizedContext<T>(
        override val remoteHost: String,
        override val callId: String?,
        override val payload: T,
        val principal: UserPrincipal
    ) : ContextAware<T>()
}
