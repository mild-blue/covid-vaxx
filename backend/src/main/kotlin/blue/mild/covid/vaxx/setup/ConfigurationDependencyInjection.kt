package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.dto.DatabaseConfigurationDto
import blue.mild.covid.vaxx.service.PatientService
import blue.mild.covid.vaxx.utils.createLogger
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import pw.forst.tools.katlib.getEnv
import pw.forst.tools.katlib.whenNull
import java.io.File

private val logger = createLogger("EnvironmentLoaderLogger")

private fun getEnvOrLogDefault(env: String, defaultValue: String) =
    getEnv(env)
        .whenNull { logger.warn { "Env variable $env not set! Using default value - $defaultValue" } } ?: defaultValue


@Suppress("SameParameterValue") // we don't care...
private fun loadVersion(defaultVersion: String = "development"): String = runCatching {
    getEnv("RELEASE_FILE_PATH")
        ?.let { File(it).readText().trim() }
        ?: defaultVersion
}.getOrNull() ?: defaultVersion

/**
 * Loads the DI container with configuration from the system environment.
 */
// TODO load all config from the file and then allow the replacement with env variables
fun DI.MainBuilder.bindConfiguration() {

    // The default values used in this configuration are for the local development.
    bind<DatabaseConfigurationDto>() with singleton {
        val db = getEnvOrLogDefault("POSTGRES_DB", "covid-vaxx")
        val dbHost = getEnvOrLogDefault("POSTGRES_HOST", "localhost:5432")

        DatabaseConfigurationDto(
            userName = getEnvOrLogDefault("POSTGRES_USER", "mildblue"),
            password = getEnvOrLogDefault("POSTGRES_PASSWORD", "mildblue-password"),
            url = "jdbc:postgresql://${dbHost}/${db}"
        )
    }

    bind<String>("version") with singleton { loadVersion("development") }

    bind<String>("frontend") with singleton {
        getEnvOrLogDefault("FRONTEND_PATH", "../frontend/dist/frontend")
    }

    bind<PatientService>("PatientService") with singleton { PatientService(instance()) }
}
