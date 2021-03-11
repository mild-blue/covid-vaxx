package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.dao.model.DatabaseSetup
import blue.mild.covid.vaxx.dto.config.CorsConfigurationDto
import blue.mild.covid.vaxx.dto.config.DatabaseConfigurationDto
import blue.mild.covid.vaxx.dto.config.JwtConfigurationDto
import blue.mild.covid.vaxx.dto.config.RateLimitConfigurationDto
import blue.mild.covid.vaxx.dto.request.CaptchaVerificationDtoIn
import blue.mild.covid.vaxx.error.installExceptionHandling
import blue.mild.covid.vaxx.extensions.determineRealIp
import blue.mild.covid.vaxx.monitoring.CALL_ID
import blue.mild.covid.vaxx.monitoring.PATH
import blue.mild.covid.vaxx.monitoring.REMOTE_HOST
import blue.mild.covid.vaxx.routes.Routes
import blue.mild.covid.vaxx.routes.registerRoutes
import blue.mild.covid.vaxx.security.auth.JwtService
import blue.mild.covid.vaxx.security.auth.RoleBasedAuthorization
import blue.mild.covid.vaxx.security.auth.registerJwtAuth
import blue.mild.covid.vaxx.security.ddos.RateLimiting
import blue.mild.covid.vaxx.utils.createLogger
import com.auth0.jwt.JWTVerifier
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.papsign.ktor.openapigen.OpenAPIGen
import com.papsign.ktor.openapigen.openAPIGen
import com.papsign.ktor.openapigen.route.apiRouting
import com.papsign.ktor.openapigen.schema.namer.DefaultSchemaNamer
import com.papsign.ktor.openapigen.schema.namer.SchemaNamer
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.CORS
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.ForwardedHeaderSupport
import io.ktor.features.XForwardedHeaderSupport
import io.ktor.features.callId
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.default
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.routing
import org.flywaydb.core.Flyway
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.slf4j.event.Level
import java.util.UUID
import kotlin.reflect.KType


private val installationLogger = createLogger("ApplicationSetup")

/**
 * Loads the application.
 */
fun Application.init() {
    // setup DI
    di {
        bindConfiguration()
        registerJwtAuth()
        registerClasses()
    }
    // now kodein is running and can be used
    installationLogger.debug { "DI container started." }
    // connect to the database
    connectDatabase()
    // configure Ktor
    installFrameworks()
    // configure static routes to serve frontend
    val staticContentPath by di().instance<String>(EnvVariables.FRONTEND_PATH)
    routing {
        static {
            files(staticContentPath)
            default("${staticContentPath}/index.html")
        }
    }
    // register routing with swagger
    apiRouting {
        registerRoutes()
    }
}

// Connect bot to the database.
private fun Application.connectDatabase() {
    val dbConfig by di().instance<DatabaseConfigurationDto>()

    installationLogger.info { "Connecting to the DB" }
    DatabaseSetup.connect(dbConfig)

    require(DatabaseSetup.isConnected()) { "It was not possible to connect to db database!" }
    installationLogger.info { "DB connected." }
    migrateDatabase(dbConfig)
}

// Migrate database using flyway.
private fun migrateDatabase(dbConfig: DatabaseConfigurationDto) {
    installationLogger.info { "Migrating database." }
    val migrateResult = Flyway
        .configure()
        .dataSource(dbConfig.url, dbConfig.userName, dbConfig.password)
        .load()
        .migrate()

    installationLogger.info {
        if (migrateResult.migrationsExecuted == 0) "No migrations necessary."
        else "Applied ${migrateResult.migrationsExecuted} migrations."
    }
}

// Configure Ktor and install necessary extensions.
private fun Application.installFrameworks() {
    installBasics()
    setupCors()
    installAuthentication()
    installMonitoring()
    installSwagger()
    installExceptionHandling()
    installRateLimiting()
}

// Install basic extensions and necessary features to the Ktor.
private fun Application.installBasics() {
    // default headers
    install(DefaultHeaders)
    // initialize Jackson
    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
        }
    }

    // as we're running behind the proxy, we take remote host from X-Forwarded-From
    install(XForwardedHeaderSupport)
    install(ForwardedHeaderSupport)
}

// Allow CORS.
private fun Application.setupCors() {
    // enable CORS if necessary
    val corsHosts by di().instance<CorsConfigurationDto>()
    val allowAndExpose: CORS.Configuration.(String) -> Unit = { headerName ->
        header(headerName)
        exposeHeader(headerName)
    }
    if (corsHosts.enableCors) {
        install(CORS) {
            allowCredentials = true
            allowNonSimpleContentTypes = true

            method(HttpMethod.Options)
            method(HttpMethod.Get)
            method(HttpMethod.Post)
            method(HttpMethod.Put)
            method(HttpMethod.Delete)

            allowAndExpose(HttpHeaders.AccessControlAllowHeaders)
            allowAndExpose(HttpHeaders.AccessControlAllowOrigin)
            allowAndExpose(HttpHeaders.ContentType)
            allowAndExpose(HttpHeaders.Authorization)
            allowAndExpose(CaptchaVerificationDtoIn.NAME)

            hosts.addAll(corsHosts.allowedHosts)
        }
    }

}

// Install authentication.
private fun Application.installAuthentication() {
    val jwtConfigurationDto by di().instance<JwtConfigurationDto>()
    val jwtVerifier by di().instance<JWTVerifier>()
    val jwtService by di().instance<JwtService>()
    // Ktor default JWT authentication
    install(Authentication) {
        jwt {
            realm = jwtConfigurationDto.realm
            verifier { jwtVerifier }
            validate { credentials -> jwtService.principalFromToken(credentials) }
        }
    }
    // Role based auth
    install(RoleBasedAuthorization)
}

// Install swagger features.
private fun Application.installSwagger() {
    val enableSwagger by di().instance<Boolean>(EnvVariables.ENABLE_SWAGGER)

    // install swagger
    install(OpenAPIGen) {
        info {
            version = "0.0.1"
            title = "Mild Blue - Covid Vaxx"
            description = "Covid Vaxx API"
            serveSwaggerUi = enableSwagger
            contact {
                name = "Mild Blue s.r.o."
                email = "covid-vaxx@mild.blue"
            }
        }
        // dto naming without package names
        replaceModule(DefaultSchemaNamer, object : SchemaNamer {
            override fun get(type: KType) = type
                .toString()
                .replace(Regex("[A-Za-z0-9_.]+")) { it.value.split(".").last() }
                .replace(Regex(">|<|, "), "_")
        })

    }
    // install swagger routes
    if (enableSwagger) {
        routing {
            // register swagger routes
            get(Routes.openApiJson) {
                call.respond(openAPIGen.api.serialize())
            }
            get(Routes.swaggerUi) {
                call.respondRedirect("/swagger-ui/index.html?url=${Routes.openApiJson}", true)
            }
        }
    }
}

// Install monitoring features and call logging.
private fun Application.installMonitoring() {
    // requests logging in debug mode + MDC tracing
    install(CallLogging) {
        // put useful information to log context
        mdc(CALL_ID) { it.callId }
        mdc(REMOTE_HOST) { it.request.determineRealIp() }
        mdc(PATH) { "${it.request.httpMethod.value} ${it.request.path()}" }

        // enable logging for all routes that are not /status
        // this filter does not influence MDC
        filter { !it.request.uri.endsWith(Routes.status) }
        level = Level.TRACE
        logger = createLogger("HttpCallLogger")
        format {
            "${it.request.determineRealIp()}: ${it.request.httpMethod.value} ${it.request.path()} -> " +
                    "${it.response.status()?.value} ${it.response.status()?.description}"
        }
    }
    // MDC call id setup
    install(CallId) {
        retrieveFromHeader("X-Request-Id")
        generate { UUID.randomUUID().toString() }
    }
}

// Install rate limiting to prevent DDoS.
private fun Application.installRateLimiting() {
    val configuration by di().instance<RateLimitConfigurationDto>()
    if (configuration.enableRateLimiting) {
        install(RateLimiting) {
            limit = configuration.rateLimit
            resetTime = configuration.rateLimitDuration
            keyExtraction = { call.request.determineRealIp() }
            requestExclusion = {
                it.httpMethod == HttpMethod.Options || it.uri.endsWith(Routes.status)
            }
        }
    }
}
