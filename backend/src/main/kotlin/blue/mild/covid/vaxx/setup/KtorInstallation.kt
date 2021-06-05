package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.dao.model.DatabaseSetup
import blue.mild.covid.vaxx.dto.config.CorsConfigurationDto
import blue.mild.covid.vaxx.dto.config.DatabaseConfigurationDto
import blue.mild.covid.vaxx.dto.config.JwtConfigurationDto
import blue.mild.covid.vaxx.dto.config.RateLimitConfigurationDto
import blue.mild.covid.vaxx.dto.response.ApplicationInformationDtoOut
import blue.mild.covid.vaxx.error.installExceptionHandling
import blue.mild.covid.vaxx.extensions.createLogger
import blue.mild.covid.vaxx.extensions.determineRealIp
import blue.mild.covid.vaxx.monitoring.AMAZON_TRACE
import blue.mild.covid.vaxx.monitoring.CALL_ID
import blue.mild.covid.vaxx.monitoring.PATH
import blue.mild.covid.vaxx.monitoring.REMOTE_HOST
import blue.mild.covid.vaxx.routes.Routes
import blue.mild.covid.vaxx.routes.registerRoutes
import blue.mild.covid.vaxx.security.auth.JwtService
import blue.mild.covid.vaxx.security.auth.RoleBasedAuthorization
import blue.mild.covid.vaxx.security.auth.registerJwtAuth
import blue.mild.covid.vaxx.security.ddos.RateLimiting
import com.auth0.jwt.JWTVerifier
import com.fasterxml.jackson.databind.ObjectMapper
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
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.default
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.jackson.JacksonConverter
import io.ktor.request.header
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.routing
import org.flywaydb.core.Flyway
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.ktor.closestDI
import org.kodein.di.ktor.di
import org.slf4j.event.Level
import java.util.UUID
import kotlin.reflect.KType
import kotlin.system.exitProcess


private val installationLogger = createLogger("ApplicationSetup")

/**
 * Loads the application.
 */
fun Application.init() = runCatching {
    // setup DI
    di {
        bindConfiguration()
        registerJwtAuth()
        registerClasses()
    }
    setupDiAwareApplication()
}.onFailure {
    installationLogger.error(it) { "It was not possible to start the application." }
    exitProcess(1)
}

/**
 * Application that already has DI context.
 */
fun Application.setupDiAwareApplication() {
    // now kodein is running and can be used

    // connect to the database
    connectDatabase()
    // configure Ktor
    installFrameworks()
    // configure routing
    installRouting()
}

private fun Application.installRouting() {
    val staticContentPath by closestDI().instance<String>(EnvVariables.FRONTEND_PATH)
    routing {
        // configure static routes to serve frontend
        static {
            files(staticContentPath)
            default("${staticContentPath}/index.html")
        }

        // configure redirects on the frontend static pages
        get("/admin") {
            call.respondRedirect("/#/admin")
        }

        get("/registration") {
            call.respondRedirect("/#/registration")
        }

        get("/info") {
            call.respondRedirect("/#/info")
        }
    }
    // register routing with swagger
    apiRouting {
        registerRoutes()
    }
}

// Connect bot to the database.
private fun Application.connectDatabase() {
    val dbConfig by closestDI().instance<DatabaseConfigurationDto>()

    installationLogger.debug { "Connecting to the DB" }
    DatabaseSetup.connect(dbConfig)

    require(DatabaseSetup.isConnected()) { "It was not possible to connect to db database!" }
    installationLogger.info { "Database is connected." }

    migrateDatabase()
}

// Migrate database using flyway.
private fun Application.migrateDatabase() {
    val shouldMigrate by closestDI().instanceOrNull<Boolean>("should-migrate")
    installationLogger.info { "Migrating database - should migrate: ${shouldMigrate}." }

    // enable migration by default
    if (shouldMigrate != false) {
        val flyway by closestDI().instance<Flyway>()
        installationLogger.info { "Migrating database - migration." }

        val migrateResult = runCatching { flyway.migrate() }
            .onFailure {
                installationLogger.error(it) { "It was not possible to migrate database! Exiting.." }
                exitProcess(1)
            }.getOrThrow()

        installationLogger.info {
            if (migrateResult.migrationsExecuted == 0) "No migrations necessary."
            else "Applied ${migrateResult.migrationsExecuted} migrations."
        }
    } else {
        installationLogger.warn { "Skipping database migration and verification, this should not be in production!" }
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
    install(DefaultHeaders) {
        header(HttpHeaders.Server, "mild-blue")
    }
    val objectMapper by closestDI().instance<ObjectMapper>()
    // initialize our own configuration for Jackson
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(objectMapper))
    }

    // as we're running behind the proxy, we take remote host from X-Forwarded-From
    install(XForwardedHeaderSupport)
    install(ForwardedHeaderSupport)
}

// Allow CORS.
private fun Application.setupCors() {
    // enable CORS if necessary
    val corsHosts by closestDI().instance<CorsConfigurationDto>()
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

            hosts.addAll(corsHosts.allowedHosts)
        }
    }

}

// Install authentication.
private fun Application.installAuthentication() {
    val jwtConfigurationDto by closestDI().instance<JwtConfigurationDto>()
    val jwtVerifier by closestDI().instance<JWTVerifier>()
    val jwtService by closestDI().instance<JwtService>()
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
    val enableSwagger by closestDI().instance<Boolean>(EnvVariables.ENABLE_SWAGGER)
    val info by closestDI().instance<ApplicationInformationDtoOut>()
    // install swagger
    install(OpenAPIGen) {

        info {
            version = info.version
            title = "Mild Blue - Covid Vaxx"
            description = "Covid Vaxx API"
            serveSwaggerUi = enableSwagger
            contact {
                name = "Mild Blue s.r.o."
                email = "support@mild.blue"
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
        // see https://docs.aws.amazon.com/elasticloadbalancing/latest/application/load-balancer-request-tracing.html
        mdc(AMAZON_TRACE) { call ->
            call.request.header("X-Amzn-Trace-Id")
        }

        val ignoredPaths = setOf(Routes.status, Routes.statusHealth)
        val ignoredMethods = setOf(HttpMethod.Options, HttpMethod.Head)
        filter {
            val path = it.request.path()
            // log just requests that goes to api
            path.startsWith("/api")
                    && !ignoredPaths.contains(path) // without ignored service paths
                    && !ignoredMethods.contains(it.request.httpMethod) // and ignored, not used, methods
        }
        level = Level.INFO // we want to log especially the results of the requests
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
    val configuration by closestDI().instance<RateLimitConfigurationDto>()
    if (configuration.enableRateLimiting) {
        install(RateLimiting) {
            limit = configuration.rateLimit
            resetTime = configuration.rateLimitDuration
            keyExtraction = { call.request.determineRealIp() }
            requestExclusion = {
                it.httpMethod == HttpMethod.Options
                        || it.uri.endsWith(Routes.status)
                        || it.uri.endsWith(Routes.statusHealth)
            }
        }
    }
}
