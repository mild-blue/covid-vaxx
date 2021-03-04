package blue.mild.covid.vaxx.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.papsign.ktor.openapigen.model.Described
import com.papsign.ktor.openapigen.model.security.HttpSecurityScheme
import com.papsign.ktor.openapigen.model.security.SecuritySchemeModel
import com.papsign.ktor.openapigen.model.security.SecuritySchemeType
import com.papsign.ktor.openapigen.modules.providers.AuthProvider
import com.papsign.ktor.openapigen.route.path.auth.OpenAPIAuthenticatedRoute
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import io.ktor.application.ApplicationCall
import io.ktor.auth.Authentication
import io.ktor.auth.Principal
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.jwt
import io.ktor.util.pipeline.PipelineContext
import java.util.Date

private inline fun NormalOpenAPIRoute.auth(route: OpenAPIAuthenticatedRoute<UserPrincipal>.() -> Unit): OpenAPIAuthenticatedRoute<UserPrincipal> {
    val authenticatedKtorRoute = this.ktorRoute.authenticate {}
    return OpenAPIAuthenticatedRoute(authenticatedKtorRoute, this.provider.child(), authProvider = JwtProvider())
        .apply { route() }
}

data class UserPrincipal(val userId: String, val name: String?) : Principal

enum class Scopes(override val description: String) : Described { Default("default") }

class JwtProvider : AuthProvider<UserPrincipal> {
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

    override suspend fun getAuth(pipeline: PipelineContext<Unit, ApplicationCall>): UserPrincipal {
        return pipeline.context.authentication.principal() ?: throw RuntimeException("No JWTPrincipal")
    }

    override fun apply(route: NormalOpenAPIRoute): OpenAPIAuthenticatedRoute<UserPrincipal> {
        val authenticatedKtorRoute = route.ktorRoute.authenticate {}
        return OpenAPIAuthenticatedRoute(authenticatedKtorRoute, route.provider.child(), this)
    }
}

const val jwtRealm: String = "example-jwt-realm"
const val jwtIssuer: String = "http://localhost:8080/api/login/$jwtRealm"
const val jwtAudience: String = "jwt-audience"

private fun installJwt(provider: Authentication.Configuration) {
    provider.apply {
        jwt {
            realm = jwtRealm
            verifier(makeJwtVerifier())
            validate { credentials ->
                UserPrincipal(
                    credentials.payload.subject,
                    credentials.payload.claims["name"]?.asString()
                )
            }
        }
    }
}

private val algorithm = Algorithm.HMAC256("secret")
private fun makeJwtVerifier(): JWTVerifier = JWT
    .require(algorithm)
    .withAudience(jwtAudience)
    .withIssuer(jwtIssuer)
    .withClaim("name", "myclaim")
    .build()

private fun generateToken(claim: String): String = JWT.create()
    .withSubject("Authentication")
    .withIssuer(jwtIssuer)
    .withSubject("prdel")
    .withClaim("name", claim)
    .withAudience(jwtAudience)
    .withExpiresAt(obtainExpirationDate())
    .withIssuedAt(Date())
    .sign(algorithm)

private fun obtainExpirationDate() = Date(System.currentTimeMillis() + 1000000)
