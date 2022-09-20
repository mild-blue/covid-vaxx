package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.dto.config.CorsConfigurationDto
import blue.mild.covid.vaxx.dto.config.CspConfigurationDto
import blue.mild.covid.vaxx.dto.config.DatabaseConfigurationDto
import blue.mild.covid.vaxx.dto.config.IsinConfigurationDto
import blue.mild.covid.vaxx.dto.config.JwtConfigurationDto
import blue.mild.covid.vaxx.dto.config.MailJetConfigurationDto
import blue.mild.covid.vaxx.dto.config.RateLimitConfigurationDto
import blue.mild.covid.vaxx.dto.config.ReCaptchaVerificationConfigurationDto
import blue.mild.covid.vaxx.dto.response.ApplicationInformationDtoOut
import blue.mild.covid.vaxx.extensions.createLogger
import blue.mild.covid.vaxx.isin.Pracovnik
import dev.forst.katlib.getEnv
import dev.forst.katlib.whenNull
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
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
            subject = getEnvOrLogDefault(EnvVariables.MAIL_SUBJECT, "Detaily k registraci na očkování")
        )
    }

    bind<ApplicationInformationDtoOut>() with singleton { ApplicationInformationDtoOut(loadVersion()) }

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
            rateLimit = getEnvOrLogDefault(EnvVariables.RATE_LIMIT, "50000").toLong(),
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

    bind<CspConfigurationDto>() with singleton {
        val enableCsp = getEnvOrLogDefault(EnvVariables.ENABLE_CSP, "true").toBoolean()
        CspConfigurationDto(enableCsp)
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

    bind<Boolean>(EnvVariables.ENABLE_ISIN_PATIENT_VALIDATION) with singleton {
        getEnvOrLogDefault(EnvVariables.ENABLE_ISIN_PATIENT_VALIDATION, "false").toBoolean()
    }

    bind<Boolean>(EnvVariables.ENABLE_ISIN_CLIENT) with singleton {
        getEnvOrLogDefault(EnvVariables.ENABLE_ISIN_CLIENT, "false").toBoolean()
    }

    bind<IsinConfigurationDto>() with singleton {
        val certPassword = getEnvOrLogDefault(EnvVariables.ISIN_CERT_PASSWORD, "")

//        // TODO certificate password decryption
//        val ksmKeyId = getEnvOrLogDefault(EnvVariables.KMS_KEY_ID, "")
//        if(!ksmKeyId.isEmpty()) {
//            val kmsClient: AWSKMS = AWSKMSClientBuilder.standard().build()
//            val encryptedCertKey: ByteBuffer = ByteBuffer.wrap(certPassword.toByteArray())
//            val req: DecryptRequest = DecryptRequest().withCiphertextBlob(encryptedCertKey).withKeyId(ksmKeyId);
//            val decryptedCertKey: ByteBuffer = kmsClient.decrypt(req).getPlaintext();
//            certPassword = decryptedCertKey.toString()
//        }

        val pracovnik = Pracovnik(
            nrzpCislo = getEnvOrLogDefault(EnvVariables.ISIN_PRACOVNIK_NRZP_CISLO, "172319367"),
            rodneCislo = "",
            // 000 je pro polikliniky - neni to placeholder
            // https://nrpzs.uzis.cz/detail-66375-clinicum-a-s.html#fndtn-detail_uzis
            pcz = getEnvOrLogDefault(EnvVariables.ISIN_PRACOVNIK_PCZ, "000")
        )
        IsinConfigurationDto(
            rootUrl = getEnvOrLogDefault(EnvVariables.ISIN_ROOT_URL, "https://apitest.uzis.cz/api/v1"),
            pracovnik = pracovnik,
            storePass = certPassword,
            certBase64 = getEnvOrLogDefault(EnvVariables.ISIN_CERT_BASE64, ""),
            storeType = getEnvOrLogDefault(EnvVariables.ISIN_STORE_TYPE, "JKS"),
            keyPass = certPassword,
            ockovaciLatkaKod = getEnvOrLogDefault(EnvVariables.ISIN_OCKOVACI_LATKA_KOD, "CO01"),
            indikaceJina = getEnvOrLogDefault(EnvVariables.ISIN_INDIKACE_JINA, "Lidé ve věku 16+")
        )
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

