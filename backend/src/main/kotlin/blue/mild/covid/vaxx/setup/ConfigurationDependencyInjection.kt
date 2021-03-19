package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.dto.config.CorsConfigurationDto
import blue.mild.covid.vaxx.dto.config.DatabaseConfigurationDto
import blue.mild.covid.vaxx.dto.config.IsinConfigurationDto
import blue.mild.covid.vaxx.dto.config.JwtConfigurationDto
import blue.mild.covid.vaxx.dto.config.MailJetConfigurationDto
import blue.mild.covid.vaxx.dto.config.RateLimitConfigurationDto
import blue.mild.covid.vaxx.dto.config.ReCaptchaVerificationConfigurationDto
import blue.mild.covid.vaxx.dto.response.ApplicationInformationDto
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
// default values explained in the code
// it is DI, long method is expected
@Suppress("MagicNumber", "LongMethod")
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

    bind<Boolean>(EnvVariables.ENABLE_MAIL_SERVICE) with singleton {
        getEnvOrLogDefault(EnvVariables.ENABLE_MAIL_SERVICE, "false").toBoolean()
    }

    bind<MailJetConfigurationDto>() with singleton {
        MailJetConfigurationDto(
            apiKey = requireEnv(EnvVariables.MAIL_JET_API_KEY),
            apiSecret = requireEnv(EnvVariables.MAIL_JET_API_SECRET),
            emailFrom = getEnvOrLogDefault(EnvVariables.MAIL_ADDRESS_FROM, "ockovani@mild.blue"),
            nameFrom = getEnvOrLogDefault(EnvVariables.MAIL_FROM, "Registrace Očkování"),
            subject = getEnvOrLogDefault(EnvVariables.MAIL_SUBJECT, "Detaily k registraci na Očkování")
        )
    }

    bind<ApplicationInformationDto>() with singleton { ApplicationInformationDto(loadVersion()) }

    bind<String>(EnvVariables.FRONTEND_PATH) with singleton {
        getEnvOrLogDefault(EnvVariables.FRONTEND_PATH, "../frontend/dist/frontend")
    }

    bind<JwtConfigurationDto>() with singleton {
        JwtConfigurationDto(
            realm = "Mild Blue Covid Vaxx",
            issuer = "vaccination.mild.blue",
            audience = "default",
            jwtExpirationInMinutes =
            // 5 days by default
            getEnvOrLogDefault(EnvVariables.JWT_EXPIRATION_IN_MINUTES, "${60 * 24 * 5}").toLong(),
            signingSecret =
            getEnvOrLogDefault(EnvVariables.JWT_SIGNING_SECRET, UUID.randomUUID().toString())
        )
    }

    bind<RateLimitConfigurationDto>() with singleton {
        RateLimitConfigurationDto(
            enableRateLimiting = getEnvOrLogDefault(EnvVariables.ENABLE_RATE_LIMITING, "true").toBoolean(),
            rateLimit = getEnvOrLogDefault(EnvVariables.RATE_LIMIT, "100").toLong(),
            rateLimitDuration = getEnvOrLogDefault(EnvVariables.RATE_LIMIT_DURATION_MINUTES, "60")
                .let { Duration.ofMinutes(it.toLong()) }
        )
    }

    bind<Boolean>(EnvVariables.ENABLE_SWAGGER) with singleton {
        getEnvOrLogDefault(EnvVariables.ENABLE_SWAGGER, "true").toBoolean()
    }

    bind<CorsConfigurationDto>() with singleton {
        val enableCors = getEnvOrLogDefault(EnvVariables.ENABLE_CORS, "true").toBoolean()
        val hosts = if (enableCors) {
            getEnvOrLogDefault(EnvVariables.CORS_ALLOWED_HOSTS, "http://localhost:4200")
                .split(",")
                .map { it.trim() }
        } else emptyList()
        CorsConfigurationDto(enableCors, hosts)
    }

    bind<Boolean>(EnvVariables.ENABLE_RECAPTCHA_VERIFICATION) with singleton {
        getEnvOrLogDefault(EnvVariables.ENABLE_RECAPTCHA_VERIFICATION, "false").toBoolean()
    }

    bind<ReCaptchaVerificationConfigurationDto>() with singleton {
        ReCaptchaVerificationConfigurationDto(
            secretKey = requireEnv(EnvVariables.RECAPTCHA_SECRET_KEY),
            googleUrl = "https://www.google.com/recaptcha/api/siteverify"
        )
    }

    bind<Boolean>(EnvVariables.ENABLE_ISIN_REGISTRATION) with singleton {
        getEnvOrLogDefault(EnvVariables.ENABLE_ISIN_REGISTRATION, "false").toBoolean()
    }

    bind<IsinConfigurationDto>() with singleton {
        TODO("Not implemented yet.")
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

