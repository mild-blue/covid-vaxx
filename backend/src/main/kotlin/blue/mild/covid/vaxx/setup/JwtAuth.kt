package blue.mild.covid.vaxx.setup

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.papsign.ktor.openapigen.annotations.Path
import com.papsign.ktor.openapigen.annotations.Response
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.annotations.properties.description.Description
import com.papsign.ktor.openapigen.model.Described
import com.papsign.ktor.openapigen.model.security.HttpSecurityScheme
import com.papsign.ktor.openapigen.model.security.SecuritySchemeModel
import com.papsign.ktor.openapigen.model.security.SecuritySchemeType
import com.papsign.ktor.openapigen.modules.providers.AuthProvider
import com.papsign.ktor.openapigen.route.apiRouting
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.OpenAPIAuthenticatedRoute
import com.papsign.ktor.openapigen.route.path.auth.get
import com.papsign.ktor.openapigen.route.path.auth.principal
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.response.respond
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.Principal
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.jwt
import io.ktor.util.pipeline.PipelineContext
import java.net.URL
import java.util.concurrent.TimeUnit


fun Application.setupJwtAuth() {
    install(io.ktor.auth.Authentication) {
        installJwt(this)
    }


    apiRouting {
        auth {
            get<StringParam, StringResponse, UserPrincipal>(
                info("String Param Endpoint", "This is a String Param Endpoint"),
                example = StringResponse("Hi")
            ) { params ->
                val (userId, name) = principal()
                respond(StringResponse("Hello $name, you submitted ${params.a}"))
            }
        }
    }

}

@Path("string/{a}")
data class StringParam(@PathParam("A simple String Param") val a: String)

@Response("A String Response")
data class StringResponse(@Description("The string value") val str: String)

val authProvider = JwtProvider();

inline fun NormalOpenAPIRoute.auth(route: OpenAPIAuthenticatedRoute<UserPrincipal>.() -> Unit): OpenAPIAuthenticatedRoute<UserPrincipal> {
    val authenticatedKtorRoute = this.ktorRoute.authenticate { }
    var openAPIAuthenticatedRoute= OpenAPIAuthenticatedRoute(authenticatedKtorRoute, this.provider.child(), authProvider = authProvider);
    return openAPIAuthenticatedRoute.apply {
        route()
    }
}


data class UserPrincipal(val userId: String, val name: String?) : Principal

class JwtProvider : AuthProvider<UserPrincipal> {
    override val security: Iterable<Iterable<AuthProvider.Security<*>>> =
        listOf(
            listOf(
                AuthProvider.Security(
                    SecuritySchemeModel(
                        SecuritySchemeType.http,
                        scheme = HttpSecurityScheme.bearer,
                        bearerFormat = "JWT",
                        name = "jwtAuth"
                    ), emptyList<Scopes>()
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

enum class Scopes(override val description: String) : Described {
    Profile("Some scope")
}

val jwtRealm : String = "example-jwt-realm"
val jwtIssuer: String = "http://localhost:9091/auth/realms/$jwtRealm"
val jwtEndpoint: String = "$jwtIssuer/protocol/openid-connect/certs"

fun installJwt (provider: Authentication.Configuration) {
    provider.apply {
        jwt {
            realm = jwtRealm
            verifier(getJwkProvider(jwtEndpoint), jwtIssuer)
//            verifier(makeJwtVerifier(jwtIssuer, "audience"))
            validate { credentials ->
                UserPrincipal(
                    credentials.payload.subject,
                    credentials.payload.claims["name"]?.asString()
                )
            }
        }
    }
}

private fun getJwkProvider(jwkEndpoint: String) =
    JwkProviderBuilder(URL(jwkEndpoint))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

private val algorithm = Algorithm.HMAC256("secret")
private fun makeJwtVerifier(issuer: String, audience: String) = JWT
    .require(algorithm)
    .withAudience(audience)
    .withIssuer(issuer)
    .build()

