package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.dao.DatabaseSetup
import blue.mild.covid.vaxx.dto.DatabaseConfigurationDto
import blue.mild.covid.vaxx.error.registerExceptionHandlers
import blue.mild.covid.vaxx.monitoring.CALL_ID
import blue.mild.covid.vaxx.routes.Routes
import blue.mild.covid.vaxx.routes.registerRoutes
import blue.mild.covid.vaxx.utils.createLogger
import com.papsign.ktor.openapigen.OpenAPIGen
import com.papsign.ktor.openapigen.openAPIGen
import com.papsign.ktor.openapigen.route.apiRouting
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
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
import org.kodein.di.LazyDI
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.slf4j.event.Level
import java.text.DateFormat
import java.util.UUID
import kotlin.random.Random


private val installationLogger = createLogger("ApplicationSetup")

/**
 * Loads the application.
 */
fun Application.init() {
    // setup DI
    di {
        bindConfiguration()
        registerClasses()
    }
    // now kodein is running and can be used
    val di by di()

    installationLogger.debug { "DI container started." }

    // connect to the database
    connectDatabase(di)

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
        registerRoutes(di)
    }
}

/**
 * Connect bot to the database.
 */
private fun connectDatabase(k: LazyDI) {
    installationLogger.info { "Connecting to the DB" }
    val dbConfig by k.instance<DatabaseConfigurationDto>()
    DatabaseSetup.connect(dbConfig)

    if (DatabaseSetup.isConnected()) {
        installationLogger.info { "DB connected." }
        migrateDatabase(dbConfig)
    } else {
        // TODO verify handling, maybe exit the App?
        installationLogger.error { "It was not possible to connect to db database! The application will start but it won't work." }
    }
}

/**
 * Migrate database using flyway.
 */
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

/**
 * Configure Ktor and install necessary extensions.
 */
private fun Application.installFrameworks() {
    // default headers
    install(DefaultHeaders)
    // initialize Jackson
    install(ContentNegotiation) {
        jackson {
            dateFormat = DateFormat.getDateTimeInstance()
        }
    }
    // enable CORS
    install(CORS) {
        // TODO correct urls
        anyHost()
        allowCredentials = true
        allowNonSimpleContentTypes = true
    }
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
    // requests logging in debug mode + MDC tracing
    install(CallLogging) {
        // put call id to the mdc
        mdc(CALL_ID) { it.callId }

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

    // register exception handling
    registerExceptionHandlers()
}
