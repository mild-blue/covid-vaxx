package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.dto.DatabaseConfigurationDto
import blue.mild.covid.vaxx.dto.JwtConfigurationDto
import blue.mild.covid.vaxx.dto.MailJetConfigurationDto
import blue.mild.covid.vaxx.dto.response.VersionDtoOut
import blue.mild.covid.vaxx.utils.createLogger
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import pw.forst.tools.katlib.getEnv
import pw.forst.tools.katlib.whenNull
import java.io.File
import java.util.UUID
import kotlin.system.exitProcess

private val logger = createLogger("EnvironmentLoaderLogger")

private fun getEnvOrLogDefault(env: String, defaultValue: String) =
    getEnv(env)
        .whenNull { logger.warn { "Env variable $env not set! Using default value - $defaultValue" } } ?: defaultValue


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

    val apiKey = getEnv("MAIL_JET_API_KEY")
    val apiSecret = getEnv("MAIL_JET_API_SECRET")
    apiKey.whenNull {
        logger.error("MAIL_JET_API_KEY env variable was not provided. Exiting")
        exitProcess(1) }
    apiSecret.whenNull {
        logger.error("MAIL_JET_API_SECRET env variable was not provided. Exiting")
        exitProcess(1)
    }

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

    bind<MailJetConfigurationDto>() with singleton {
        MailJetConfigurationDto(
            apiKey = apiKey?: "",
            apiSecret = apiSecret?: "",
            emailFrom = getEnvOrLogDefault("MAIL_ADDRESS_FROM", "services@mild.blue")
        )
    }

    bind<VersionDtoOut>() with singleton { VersionDtoOut(loadVersion()) }

    bind<String>("frontend") with singleton {
        getEnvOrLogDefault("FRONTEND_PATH", "../frontend/dist/frontend")
    }

    // TODO load this from the env / config
    bind<JwtConfigurationDto>() with singleton {
        JwtConfigurationDto(
            realm = "Mild Blue Covid Vaxx",
            issuer = "vaccination.mild.blue",
            audience = "default",
            registeredUserJwtExpirationInMinutes = 60 * 24 * 5, // 5 days
            patientUserJwtExpirationInMinutes = 15,
            signingSecret = UUID.randomUUID().toString()
        )
    }
}
