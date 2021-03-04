package blue.mild.covid.vaxx.auth

import com.papsign.ktor.openapigen.model.Described
import com.papsign.ktor.openapigen.model.security.HttpSecurityScheme
import com.papsign.ktor.openapigen.model.security.SecuritySchemeModel
import com.papsign.ktor.openapigen.model.security.SecuritySchemeType
import com.papsign.ktor.openapigen.modules.providers.AuthProvider
import com.papsign.ktor.openapigen.route.path.auth.OpenAPIAuthenticatedRoute
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import io.ktor.application.ApplicationCall
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.util.pipeline.PipelineContext

class JwtAuthProvider : AuthProvider<UserPrincipal> {

    enum class Scopes(override val description: String) : Described { Default("default") }

    override val security: Iterable<Iterable<AuthProvider.Security<*>>> =
        listOf(
            listOf(
                AuthProvider.Security(
                    SecuritySchemeModel(
                        type = SecuritySchemeType.http,
                        scheme = HttpSecurityScheme.bearer,
                        bearerFormat = "JWT",
                        name = "jwtAuth"
                    ),
                    listOf(Scopes.Default)
                )
            )
        )

    override suspend fun getAuth(pipeline: PipelineContext<Unit, ApplicationCall>): UserPrincipal =
        pipeline.context.authentication.principal()
            ?: throw AuthorizationException("No JWTPrincipal")

    override fun apply(route: NormalOpenAPIRoute): OpenAPIAuthenticatedRoute<UserPrincipal> {
        val authenticatedKtorRoute = route.ktorRoute.authenticate {}
        return OpenAPIAuthenticatedRoute(authenticatedKtorRoute, route.provider.child(), this)
    }
}
