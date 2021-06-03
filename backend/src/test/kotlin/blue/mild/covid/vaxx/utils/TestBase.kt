package blue.mild.covid.vaxx.utils

import blue.mild.covid.vaxx.dao.model.DatabaseSetup
import blue.mild.covid.vaxx.dto.config.DatabaseConfigurationDto
import blue.mild.covid.vaxx.security.auth.JwtService
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.registerJwtAuth
import blue.mild.covid.vaxx.setup.bindConfiguration
import blue.mild.covid.vaxx.setup.registerClasses
import blue.mild.covid.vaxx.setup.setupDiAwareApplication
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import mu.KLogging
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.ktor.di
import org.kodein.di.singleton
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


/**
 * Test base that has access to initialized dependency injection.
 *
 * Use [overrideDIContainer] to inject and override additional dependencies.
 */
open class DiAwareTestBase {
    protected val rootDI = DI(allowSilentOverride = true) {
        bindConfiguration()
        registerJwtAuth()
        registerClasses()

        // disable migration on startup, instead migrate during tests
        bind<Boolean>("should-migrate") with singleton { false }

        overrideDIContainer()?.let { extend(it, allowOverride = true) }
    }

    /**
     * Override this if you want to add additional bindings or if you want to override
     * some instances from the base DI container.
     */
    protected open fun overrideDIContainer(): DI? = null
}

/**
 * Base class that has access to the database.
 *
 * The database is cleaned and migrated before each test.
 */
open class DatabaseTestBase(private val shouldSetupDatabase: Boolean = true) : DiAwareTestBase() {

    private val flyway by rootDI.instance<Flyway>()

    @BeforeEach
    fun beforeEach() {
        if (shouldSetupDatabase) {
            val dbConfig by rootDI.instance<DatabaseConfigurationDto>()

            DatabaseSetup.connect(dbConfig)

            require(DatabaseSetup.isConnected()) { "It was not possible to connect to db database!" }

            flyway.clean()
            flyway.migrate()

            populateDatabase(rootDI)
        }
    }

    @AfterEach
    fun afterEach() {
        if (shouldSetupDatabase) {
            flyway.clean()
        }
    }

    /**
     * Override this when you need to add additional data before the each test.
     * This method is called when the database is fully migrated.
     *
     * Executed only when [shouldSetupDatabase] is true.
     */
    protected open fun populateDatabase(di: DI) {}
}

/**
 * Base class with access to running Ktor server. Parameter needsDatabase indicates
 * whether the test class needs access to the database or not.
 *
 * Use [withTestApplication] to access the server resource.
 *
 * Examples available: [here](https://github.com/ktorio/ktor-documentation/tree/master/codeSnippets/snippets/testable)
 */
open class ServerTestBase(needsDatabase: Boolean = true) : DatabaseTestBase(needsDatabase) {

    protected companion object : KLogging()

    protected val mapper by rootDI.instance<ObjectMapper>()
    protected val jwtService by rootDI.instance<JwtService>()

    protected fun <R> withTestApplication(test: TestApplicationEngine.() -> R) {
        withTestApplication(
            {
                di { extend(rootDI, allowOverride = true) }
                setupDiAwareApplication()
            },
            test
        )
    }

    protected fun TestApplicationEngine.closestDI() = application.closestDI()

    protected fun TestApplicationCall.expectStatus(status: HttpStatusCode) = assertEquals(status, response.status())

    protected inline fun <reified T> TestApplicationRequest.jsonBody(data: T) {
        setBody(mapper.writeValueAsString(data))
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    }

    protected inline fun <reified T> TestApplicationCall.receive(): T =
        assertNotNull(receiveOrNull<T>(), "Received content was null!")

    protected inline fun <reified T> TestApplicationCall.receiveOrNull(): T? {
        val content = response.content ?: return null
        logger.info { "received content:\n${content}" }
        return mapper.readValue(content)
    }

    private val defaultPrincipal = UserPrincipal(
        userId = DatabaseData.admin.id,
        userRole = DatabaseData.admin.role,
        vaccineSerialNumber = "",
        nurseId = null
    )

    protected fun TestApplicationRequest.authorize(principal: UserPrincipal = defaultPrincipal) {
        val token = jwtService.generateToken(principal).token
        addHeader(HttpHeaders.Authorization, "Bearer $token")
    }
}
