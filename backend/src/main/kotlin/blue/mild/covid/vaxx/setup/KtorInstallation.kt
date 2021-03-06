package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.auth.JwtService
import blue.mild.covid.vaxx.auth.RoleBasedAuthorization
import blue.mild.covid.vaxx.auth.registerJwtAuth
import blue.mild.covid.vaxx.dao.DatabaseSetup
import blue.mild.covid.vaxx.dto.DatabaseConfigurationDto
import blue.mild.covid.vaxx.dto.JwtConfigurationDto
import blue.mild.covid.vaxx.error.installExceptionHandling
import blue.mild.covid.vaxx.extensions.determineRealIp
import blue.mild.covid.vaxx.monitoring.CALL_ID
import blue.mild.covid.vaxx.monitoring.REMOTE_HOST
import blue.mild.covid.vaxx.routes.Routes
import blue.mild.covid.vaxx.routes.registerRoutes
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
import io.ktor.http.content.default
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.jackson.jackson
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
import kotlin.random.Random
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
    val frontendBasePath by di().instance<String>("frontend")
    routing {
        static {
            files(frontendBasePath)
            default("$frontendBasePath/index.html")
        }
    }
    // register routing with swagger
    apiRouting {
        registerRoutes()
    }
}

// Connect bot to the database.
private fun Application.connectDatabase() {
    installationLogger.info { "Connecting to the DB" }
    val dbConfig by di().instance<DatabaseConfigurationDto>()
    DatabaseSetup.connect(dbConfig)

    if (DatabaseSetup.isConnected()) {
        installationLogger.info { "DB connected." }
        migrateDatabase(dbConfig)
    } else {
        // TODO verify handling, maybe exit the App?
        installationLogger.error { "It was not possible to connect to db database! The application will start but it won't work." }
    }
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
    installAuthentication()
    installMonitoring()
    installSwagger()
    installExceptionHandling()
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
    // enable CORS
    install(CORS) {
        // TODO #87 correct urls
        anyHost()
        host("localhost:4200")
        header("Authorization")
        allowCredentials = true
        allowNonSimpleContentTypes = true
    }
    // as we're running behind the proxy, we take remote host from X-Forwarded-From
    install(XForwardedHeaderSupport)
    install(ForwardedHeaderSupport)
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
    // install swagger
    install(OpenAPIGen) {
        info {
            version = "0.0.1"
            title = "Mild Blue - Covid Vaxx"
            description = "Covid Vaxx API"
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
    // TODO maybe conditional once we're in the production
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

// Install monitoring features and call logging.
private fun Application.installMonitoring() {
    // requests logging in debug mode + MDC tracing
    install(CallLogging) {
        // put call id to the mdc
        mdc(CALL_ID) { it.callId }
        mdc(REMOTE_HOST) {
            it.request.determineRealIp()
        }

        // enable logging for all routes that are not /status
        // this filter does not influence MDC
        filter { it.request.uri != Routes.status }
        level = Level.DEBUG
        logger = createLogger("HttpCallLogger")
    }
    // MDC call id setup
    install(CallId) {
        retrieveFromHeader("X-Request-Id")
        generate {
            // this is not "secure" as Random does not have necessary entropy (we don't need that here)
            // but it's way faster, see https://stackoverflow.com/a/14534126/7169288
            UUID(Random.nextLong(), Random.nextLong()).toString()
        }
    }
}
