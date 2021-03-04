package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.auth.AuthorizedRouteSelector
import blue.mild.covid.vaxx.auth.RoleBasedAuthorization
import blue.mild.covid.vaxx.auth.withRole
import blue.mild.covid.vaxx.setup.TestServerWithJwtAuth.auth
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.papsign.ktor.openapigen.OpenAPIGen
import com.papsign.ktor.openapigen.annotations.Path
import com.papsign.ktor.openapigen.annotations.Response
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.annotations.properties.description.Description
import com.papsign.ktor.openapigen.model.Described
import com.papsign.ktor.openapigen.model.security.HttpSecurityScheme
import com.papsign.ktor.openapigen.model.security.SecuritySchemeModel
import com.papsign.ktor.openapigen.model.security.SecuritySchemeType
import com.papsign.ktor.openapigen.modules.providers.AuthProvider
import com.papsign.ktor.openapigen.openAPIGen
import com.papsign.ktor.openapigen.route.apiRouting
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.OpenAPIAuthenticatedRoute
import com.papsign.ktor.openapigen.route.path.auth.get
import com.papsign.ktor.openapigen.route.path.auth.principal
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.feature
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.AuthenticationRouteSelector
import io.ktor.auth.Principal
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.application
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.pipeline.PipelineContext
import java.util.Date

object TestServerWithJwtAuth {

    @JvmStatic
    fun main(args: Array<String>) {
        embeddedServer(Netty, 8080, "localhost") {
            testServerWithJwtAuth()
        }.start(true)
    }


    private fun Application.testServerWithJwtAuth() {
        //define basic OpenAPI info
        install(OpenAPIGen) {
            info {
                version = "0.1"
                title = "Test API"
                description = "The Test API"
                contact {
                    name = "Support"
                    email = "support@test.com"
                }
            }
        }

        install(ContentNegotiation) {
            jackson {

            }
        }

        install(Authentication) {
            installJwt(this)
        }

        install(RoleBasedAuthorization) {
            getRoles { setOf("ne") }
        }


        // serve OpenAPI and redirect from root
        routing {
            get("/openapi.json") {
                call.respond(application.openAPIGen.api.serialize())
            }

            get("/") {
                call.respondRedirect("/swagger-ui/index.html?url=/openapi.json", true)
            }
        }

        apiRouting {
            route("/login").get<Unit, StringResponse> {
                respond(StringResponse(generateToken("myclaim")))
            }

            auth {
                route("string").get<StringParam, StringResponse, UserPrincipal>(
                    info("String Param Endpoint", "This is a String Param Endpoint"),
                    example = StringResponse("Hi")
                ) { params ->
                    val (userId, name) = principal()
                    respond(StringResponse("Hello $name, you submitted ${params.a}"))
                }
            }
        }
    }

    @Path("{a}")
    data class StringParam(@PathParam("A simple String Param") val a: String)

    @Response("A String Response")
    data class StringResponse(@Description("The string value") val str: String)


    private inline fun NormalOpenAPIRoute.auth(route: OpenAPIAuthenticatedRoute<UserPrincipal>.() -> Unit): OpenAPIAuthenticatedRoute<UserPrincipal> {
        val authenticatedKtorRoute = this.ktorRoute.authenticate { }

        val authorizedRoute = authenticatedKtorRoute.createChild(AuthorizedRouteSelector("allOf( nene )"))
        authorizedRoute.application.feature(RoleBasedAuthorization).interceptPipeline(authorizedRoute, null, setOf("ne"), null)

        return OpenAPIAuthenticatedRoute(authorizedRoute, this.provider.child(), authProvider = JwtProvider())
            .apply {
                route()
            }
    }

    data class UserPrincipal(val userId: String, val name: String) : Principal

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
                        credentials.payload.claims["name"]!!.asString()
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

}
