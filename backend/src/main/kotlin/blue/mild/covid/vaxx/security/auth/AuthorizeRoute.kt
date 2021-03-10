package blue.mild.covid.vaxx.security.auth

import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.extensions.di
import com.papsign.ktor.openapigen.route.path.auth.OpenAPIAuthenticatedRoute
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import io.ktor.application.feature
import io.ktor.auth.authenticate
import io.ktor.routing.application
import io.ktor.util.pipeline.ContextDsl
import org.kodein.di.instance

/**
 * Require authorization for the given route. If [requireOneOf] is specified the route will require
 * one of the roles to be present in the JWT claims.
 */
@ContextDsl // just to make it fancy in Idea
fun NormalOpenAPIRoute.authorizeRoute(
    requireOneOf: Set<UserRole> = emptySet(),
    route: OpenAPIAuthenticatedRoute<UserPrincipal>.() -> Unit
): OpenAPIAuthenticatedRoute<UserPrincipal> {
    // create authenticated Ktor route
    val authenticatedKtorRoute = ktorRoute.authenticate { }
    val authorizedKtorRoute = if (requireOneOf.isNotEmpty()) {
        // inject new child route that requires additional authorization
        authenticatedKtorRoute.createChild(AuthorizedRouteSelector("oneOf (${requireOneOf.joinToString(" ")})"))
            .also {
                // add role based authorization to the child pipeline
                it.application.feature(RoleBasedAuthorization).interceptPipeline(it, requireOneOf)
            }
    } else {
        authenticatedKtorRoute
    }
    // obtain provider from the DI container
    val jwtProvider by di().instance<JwtAuthProvider>()
    // and register this route in the swagger
    return OpenAPIAuthenticatedRoute(authorizedKtorRoute, provider.child(), jwtProvider).apply { route() }
}


