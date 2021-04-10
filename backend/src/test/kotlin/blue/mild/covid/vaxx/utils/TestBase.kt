package blue.mild.covid.vaxx.utils

import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.security.auth.JwtService
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.registerJwtAuth
import blue.mild.covid.vaxx.setup.bindConfiguration
import blue.mild.covid.vaxx.setup.registerClasses
import blue.mild.covid.vaxx.setup.setupDiAwareApplication
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpHeaders
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.withTestApplication
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.singleton
import java.util.UUID
import kotlin.test.assertNotNull


/**
 * Test base that has access to initialized dependency injection.
 *
 * Use [additionalModules] to inject and override additional dependencies.
 */
open class DiAwareTestBase {
    protected val di by lazy {
        DI(allowSilentOverride = true) {
            bindConfiguration()
            registerJwtAuth()
            registerClasses()

            // disable migration on startup, instead migrate during tests
            bind<Boolean>("should-migrate") with singleton { false }

            additionalModules()?.let { extend(it, allowOverride = true) }
        }
    }

    protected open fun additionalModules(): DI? = null
}

/**
 * Base class that has access to the database.
 *
 * The database is cleaned and migrated before each test.
 */
open class DatabaseTestBase(private val shouldSetupDatabase: Boolean = true) : DiAwareTestBase() {

    private val flyway by di.instance<Flyway>()

    @BeforeEach
    fun beforeEach() {
        if (shouldSetupDatabase) {
            flyway.clean()
            flyway.migrate()
        }
    }

    @AfterEach
    fun afterEach() {
        if (shouldSetupDatabase) {
            flyway.clean()
        }
    }
}

/**
 * Base class with access to running Ktor server. Parameter needsDatabase indicates
 * whether the test class needs access to the database or not.
 *
 * Use [withTestApplication] to access the server resource.
 *
 * Examples available: https://github.com/ktorio/ktor-documentation/tree/master/codeSnippets/snippets/testable
 */
open class ServerTestBase(needsDatabase: Boolean = true) : DatabaseTestBase(needsDatabase) {

    protected fun <R> withTestApplication(test: TestApplicationEngine.() -> R) {
        withTestApplication(
            {
                di {
                    extend(di, allowOverride = true)
                }
                setupDiAwareApplication()
            },
            test
        )
    }

    protected fun TestApplicationEngine.di() = this.application.di()

    protected inline fun <reified T> TestApplicationCall.receive(): T =
        assertNotNull(receiveOrNull<T>(), "Received content was null!")

    protected inline fun <reified T> TestApplicationCall.receiveOrNull(): T? =
        response.content?.let {
            val mapper by di().instance<ObjectMapper>()
            mapper.readValue(it)
        }

    private val defaultPrincipal = UserPrincipal(
        userId = UUID.fromString("c3858476-3934-4727-82f5-f9d42cea4adb"),
        userRole = UserRole.ADMIN,
        vaccineSerialNumber = "",
        nurseId = null
    )

    protected fun TestApplicationRequest.authorize(principal: UserPrincipal = defaultPrincipal) {
        val jwtService by di.instance<JwtService>()
        val token = jwtService.generateToken(principal).token
        addHeader(HttpHeaders.Authorization, "Bearer $token")
    }
}
