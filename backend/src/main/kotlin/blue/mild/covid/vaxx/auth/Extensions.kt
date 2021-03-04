package blue.mild.covid.vaxx.auth

import blue.mild.covid.vaxx.dao.UserRole
import com.papsign.ktor.openapigen.route.path.auth.OpenAPIAuthenticatedRoute
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import io.ktor.application.feature
import io.ktor.auth.authenticate
import io.ktor.routing.RouteSelector
import io.ktor.routing.RouteSelectorEvaluation
import io.ktor.routing.RoutingResolveContext
import io.ktor.routing.application

inline fun NormalOpenAPIRoute.authorizeRoute(
    requireOneOf: Set<UserRole> = emptySet(),
    crossinline route: OpenAPIAuthenticatedRoute<UserPrincipal>.() -> Unit
): OpenAPIAuthenticatedRoute<UserPrincipal> {
    val authenticatedKtorRoute = ktorRoute.authenticate { }

    val authorizedKtorRoute = if (requireOneOf.isNotEmpty()) {
        authenticatedKtorRoute.createChild(AuthorizedRouteSelector("oneOf (${requireOneOf.joinToString(" ")})"))
            .also { it.application.feature(RoleBasedAuthorization).interceptPipeline(it, requireOneOf) }
    } else {
        authenticatedKtorRoute
    }

    return OpenAPIAuthenticatedRoute(authorizedKtorRoute, provider.child(), authProvider = JwtAuthProvider()).apply { route() }
}


class AuthorizedRouteSelector(private val description: String) : RouteSelector(RouteSelectorEvaluation.qualityConstant) {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) = RouteSelectorEvaluation.Constant

    override fun toString(): String = "(authorize ${description})"
}

