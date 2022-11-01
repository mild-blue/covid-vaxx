package blue.mild.covid.vaxx.security.auth

import blue.mild.covid.vaxx.dao.model.UserRole
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.authentication
import io.ktor.server.response.respond

/**
 * Plugin for a scope that allows role based authorization.
 */
val RoleBasedAuthorization = createRouteScopedPlugin(
    "RoleBasedAuthorization",
    ::RoleBasedAuthorizationConfig
) {
    on(AuthenticationChecked) { call ->
        val principal = call.authentication.principal<UserPrincipal>()
        if (principal == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@on
        }

        if (pluginConfig.anyOf.isNotEmpty() && principal.userRole !in pluginConfig.anyOf) {
            pluginConfig.unauthorizedResponse(call)
        }
    }
}

/**
 * Configuration for [RoleBasedAuthorization].
 */
@Suppress("DataClassShouldBeImmutable") // in this case it has to, because it is plugin installation
data class RoleBasedAuthorizationConfig(
    /**
     * User is let in if it has at least one of the roles in this set.
     */
    var anyOf: Set<UserRole> = emptySet(),
    /**
     * Block that is executed when the user does not have required role.
     */
    var unauthorizedResponse: suspend (ApplicationCall) -> Unit = {
        it.respond(HttpStatusCode.Forbidden)
    }
)
