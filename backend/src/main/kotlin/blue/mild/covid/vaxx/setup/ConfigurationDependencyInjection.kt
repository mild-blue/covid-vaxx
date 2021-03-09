package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.dto.config.CorsConfigurationDto
import blue.mild.covid.vaxx.dto.config.DatabaseConfigurationDto
import blue.mild.covid.vaxx.dto.config.EnableMailServiceDto
import blue.mild.covid.vaxx.dto.config.JwtConfigurationDto
import blue.mild.covid.vaxx.dto.config.MailJetConfigurationDto
import blue.mild.covid.vaxx.dto.config.RateLimitConfigurationDto
import blue.mild.covid.vaxx.dto.config.StaticContentConfigurationDto
import blue.mild.covid.vaxx.dto.config.SwaggerConfigurationDto
import blue.mild.covid.vaxx.dto.response.VersionDtoOut
import blue.mild.covid.vaxx.utils.createLogger
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import pw.forst.tools.katlib.getEnv
import pw.forst.tools.katlib.whenNull
import java.io.File
import java.time.Duration
import java.util.UUID

/**
 * Loads the DI container with configuration from the system environment.
 */
// TODO load all config from the file and then allow the replacement with env variables
fun DI.MainBuilder.bindConfiguration() {
    // The default values used in this configuration are for the local development.
    bind<DatabaseConfigurationDto>() with singleton {
        val db = getEnvOrLogDefault(EnvVariables.POSTGRES_DB, "covid-vaxx")
        val dbHost = getEnvOrLogDefault(EnvVariables.POSTGRES_HOST, "localhost:5432")

        DatabaseConfigurationDto(
            userName = getEnvOrLogDefault(EnvVariables.POSTGRES_USER, "mildblue"),
            password = getEnvOrLogDefault(EnvVariables.POSTGRES_PASSWORD, "mildblue-password"),
            url = "jdbc:postgresql://${dbHost}/${db}"
        )
    }

    bind<EnableMailServiceDto>() with singleton {
        EnableMailServiceDto(getEnvOrLogDefault(EnvVariables.ENABLE_MAIL_SERVICE, "false").toBoolean())
    }

    bind<MailJetConfigurationDto>() with singleton {
        MailJetConfigurationDto(
            apiKey = requireEnv(EnvVariables.MAIL_JET_API_KEY),
            apiSecret = requireEnv(EnvVariables.MAIL_JET_API_SECRET),
            emailFrom = getEnvOrLogDefault(EnvVariables.MAIL_ADDRESS_FROM, "services@mild.blue"),
            nameFrom = getEnvOrLogDefault(EnvVariables.NAME_FROM, "Registrace Očkování")
        )
    }

    bind<VersionDtoOut>() with singleton { VersionDtoOut(loadVersion()) }

    bind<StaticContentConfigurationDto>() with singleton {
        StaticContentConfigurationDto(getEnvOrLogDefault(EnvVariables.FRONTEND_PATH, "../frontend/dist/frontend"))
    }

    bind<JwtConfigurationDto>() with singleton {
        JwtConfigurationDto(
            realm = "Mild Blue Covid Vaxx",
            issuer = "vaccination.mild.blue",
            audience = "default",
            registeredUserJwtExpirationInMinutes =
            getEnvOrLogDefault(EnvVariables.JWT_EXPIRATION_REGISTERED_USER_MINUTES, "${60 * 24 * 5}").toInt(),
            patientUserJwtExpirationInMinutes =
            getEnvOrLogDefault(EnvVariables.JWT_EXPIRATION_PATIENT_MINUTES, "30").toInt(),
            signingSecret =
            getEnvOrLogDefault(EnvVariables.JWT_SIGNING_SECRET, UUID.randomUUID().toString())
        )
    }

    bind<RateLimitConfigurationDto>() with singleton {
        RateLimitConfigurationDto(
            rateLimit =
            getEnvOrLogDefault(EnvVariables.RATE_LIMIT, "100").toLong(),
            rateLimitDuration =
            getEnvOrLogDefault(EnvVariables.RATE_LIMIT_DURATION_MINUTES, "60")
                .let { Duration.ofMinutes(it.toLong()) }
        )
    }

    bind<SwaggerConfigurationDto>() with singleton {
        SwaggerConfigurationDto(getEnvOrLogDefault(EnvVariables.ENABLE_SWAGGER, "true").toBoolean())
    }

    bind<CorsConfigurationDto>() with singleton {
        val hosts = getEnvOrLogDefault(EnvVariables.CORS_ALLOWED_HOSTS, "http://localhost:4200")
            .split(",")
            .map { it.trim() }

        val enableCors = getEnvOrLogDefault(EnvVariables.ENABLE_CORS, "${hosts.isNotEmpty()}").toBoolean()
        CorsConfigurationDto(enableCors, hosts)
    }
}

private fun getEnvOrLogDefault(env: EnvVariables, defaultValue: String) =
    getEnv(env.name)
        .whenNull { createLogger("EnvironmentLoaderLogger").warn { "Env variable $env not set! Using default value - $defaultValue" } }
        ?: defaultValue

private fun requireEnv(env: EnvVariables) =
    requireNotNull(getEnv(env.name)) { "${env.name} env variable was not provided. Exiting." }

private fun loadVersion(defaultVersion: String = "development"): String = runCatching {
    getEnv(EnvVariables.RELEASE_FILE_PATH.name)?.let { File(it).readText().trim() } ?: defaultVersion
}.getOrNull() ?: defaultVersion

