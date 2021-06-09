package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.dao.repository.DataCorrectnessRepository
import blue.mild.covid.vaxx.dao.repository.LocationRepository
import blue.mild.covid.vaxx.dao.repository.NurseRepository
import blue.mild.covid.vaxx.dao.repository.PatientRepository
import blue.mild.covid.vaxx.dao.repository.UserRepository
import blue.mild.covid.vaxx.dao.repository.VaccinationRepository
import blue.mild.covid.vaxx.dao.repository.VaccinationSlotRepository
import blue.mild.covid.vaxx.dto.config.DatabaseConfigurationDto
import blue.mild.covid.vaxx.dto.config.IsinConfigurationDto
import blue.mild.covid.vaxx.dto.config.MailJetConfigurationDto
import blue.mild.covid.vaxx.extensions.createLogger
import blue.mild.covid.vaxx.security.ddos.CaptchaVerificationService
import blue.mild.covid.vaxx.security.ddos.RequestVerificationService
import blue.mild.covid.vaxx.service.DataCorrectnessService
import blue.mild.covid.vaxx.service.IsinRegistrationService
import blue.mild.covid.vaxx.service.IsinService
import blue.mild.covid.vaxx.service.IsinValidationService
import blue.mild.covid.vaxx.service.LocationService
import blue.mild.covid.vaxx.service.MailJetEmailService
import blue.mild.covid.vaxx.service.MailService
import blue.mild.covid.vaxx.service.MedicalRegistrationService
import blue.mild.covid.vaxx.service.PasswordHashProvider
import blue.mild.covid.vaxx.service.PatientService
import blue.mild.covid.vaxx.service.PatientValidationService
import blue.mild.covid.vaxx.service.QuestionService
import blue.mild.covid.vaxx.service.SystemStatisticsService
import blue.mild.covid.vaxx.service.UserService
import blue.mild.covid.vaxx.service.VaccinationService
import blue.mild.covid.vaxx.service.VaccinationSlotService
import blue.mild.covid.vaxx.service.ValidationService
import blue.mild.covid.vaxx.service.dummy.DummyMailService
import blue.mild.covid.vaxx.service.dummy.DummyMedicalRegistrationService
import blue.mild.covid.vaxx.service.dummy.DummyPatientValidationService
import blue.mild.covid.vaxx.service.dummy.DummyRequestVerificationService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
import freemarker.template.Configuration
import freemarker.template.Version
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.apache.ApacheEngineConfig
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import org.apache.http.ssl.SSLContextBuilder
import org.flywaydb.core.Flyway
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import pw.forst.katlib.InstantTimeProvider
import pw.forst.katlib.TimeProvider
import pw.forst.katlib.jacksonMapper
import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.time.Instant
import java.util.Base64

/**
 * Register instances that are created only when needed.
 */
@Suppress("LongMethod") // this is a DI container, that's fine
fun DI.MainBuilder.registerClasses() {
    bind<Flyway>() with singleton {
        val dbConfig = instance<DatabaseConfigurationDto>()
        Flyway
            .configure()
            .dataSource(dbConfig.url, dbConfig.userName, dbConfig.password)
            .load()
    }

    bind<ObjectMapper>() with singleton {
        jacksonMapper().apply {
            registerModule(JavaTimeModule())
            // use ie. 2021-03-15T13:55:39.813985Z instead of 1615842349.47899
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    bind<LocationRepository>() with singleton { LocationRepository() }
    bind<VaccinationSlotRepository>() with singleton { VaccinationSlotRepository(instance()) }
    bind<PatientRepository>() with singleton { PatientRepository(instance()) }
    bind<UserRepository>() with singleton { UserRepository() }
    bind<DataCorrectnessRepository>() with singleton { DataCorrectnessRepository() }
    bind<NurseRepository>() with singleton { NurseRepository() }
    bind<VaccinationRepository>() with singleton { VaccinationRepository() }

    bind<PasswordHashProvider>() with singleton { PasswordHashProvider() }
    bind<TimeProvider<Instant>>() with singleton { InstantTimeProvider }

    bind<QuestionService>() with singleton { QuestionService() }
    bind<ValidationService>() with singleton { ValidationService(instance()) }
    bind<LocationService>() with singleton { LocationService(instance()) }
    bind<VaccinationSlotService>() with singleton { VaccinationSlotService(instance(), instance()) }
    bind<PatientService>() with singleton { PatientService(instance(), instance()) }
    bind<UserService>() with singleton { UserService(instance(), instance()) }
    bind<VaccinationService>() with singleton { VaccinationService(instance()) }
    bind<DataCorrectnessService>() with singleton { DataCorrectnessService(instance(), instance()) }
    bind<MailJetEmailService>() with singleton { MailJetEmailService(instance(), instance(), instance(), instance()) }
    bind<SystemStatisticsService>() with singleton { SystemStatisticsService() }

    bind<MailjetClient>() with singleton {
        val mailJetConfig = instance<MailJetConfigurationDto>()
        MailjetClient(
            ClientOptions.builder()
                .apiKey(mailJetConfig.apiKey)
                .apiSecretKey(mailJetConfig.apiSecret)
                .build(),
        )
    }
    bind<DummyMailService>() with singleton { DummyMailService() }

    bind<Configuration>() with singleton {
        @Suppress("MagicNumber") // version specification of the client.
        Configuration(Version(2, 3, 31)).apply {
            setClassForTemplateLoading(MailJetEmailService::class.java, "/templates")
        }
    }

    bind<HttpClient>() with singleton {
        val mapper = instance<ObjectMapper>()

        HttpClient(Apache) {
            install(JsonFeature) {
                serializer = JacksonSerializer(mapper)
            }
        }
    }

    val isinHttpClientTag = "isin"
    bind<HttpClient>(isinHttpClientTag) with singleton {
        val mapper = instance<ObjectMapper>()

        @Suppress("MagicNumber") // carefully chosen constant
        val isinTimeOutMillis = 15000L
        HttpClient(Apache) {
            install(JsonFeature) {
                serializer = JacksonSerializer(mapper)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = isinTimeOutMillis
            }

            configureCertificates(instance())
        }
    }


    bind<CaptchaVerificationService>() with singleton { CaptchaVerificationService(instance(), instance()) }
    bind<DummyRequestVerificationService>() with singleton { DummyRequestVerificationService() }

    bind<IsinRegistrationService>() with singleton { IsinRegistrationService(instance(), instance(), instance(), instance()) }
    bind<DummyMedicalRegistrationService>() with singleton { DummyMedicalRegistrationService() }

    bind<IsinService>() with singleton { IsinService(instance(), instance(isinHttpClientTag)) } // TODOO: do not use isin client in tests

    bind<IsinValidationService>() with singleton { IsinValidationService(instance()) }
    bind<DummyPatientValidationService>() with singleton { DummyPatientValidationService() }

    // select implementations based on the feature flags
    registerProductionOrDummy<MedicalRegistrationService, IsinRegistrationService, DummyMedicalRegistrationService>(
        EnvVariables.ENABLE_ISIN_REGISTRATION
    )
    registerProductionOrDummy<RequestVerificationService, CaptchaVerificationService, DummyRequestVerificationService>(
        EnvVariables.ENABLE_RECAPTCHA_VERIFICATION
    )
    registerProductionOrDummy<MailService, MailJetEmailService, DummyMailService>(
        EnvVariables.ENABLE_MAIL_SERVICE
    )

    registerProductionOrDummy<PatientValidationService, IsinValidationService, DummyPatientValidationService>(
        EnvVariables.ENABLE_ISIN_PATIENT_VALIDATION
    )
}

internal val diLogger = createLogger("DependencyInjection")

/**
 * Determines what implementation should be used - either the production one [TProd]
 * or the dummy [TDummy] implementation based on the environmental variable [enableProdEnv].
 */
private inline fun <reified TInterface : Any, reified TProd : TInterface, reified TDummy : TInterface>
        DI.MainBuilder.registerProductionOrDummy(enableProdEnv: EnvVariables) {
    bind<TInterface>() with singleton {
        if (instance(enableProdEnv)) {
            instance<TProd>()
        } else {
            diLogger.warn { "Using ${TDummy::class.simpleName}! This should not be in production!" }
            instance<TDummy>()
        }
    }
}

private fun HttpClientConfig<ApacheEngineConfig>.configureCertificates(config: IsinConfigurationDto) {
    val keystore = runCatching {
        ByteArrayInputStream(Base64.getDecoder().decode(config.certBase64)).use {
            KeyStore.getInstance(config.storeType).apply {
                load(it, config.storePass.toCharArray())
            }
        }
    }.onFailure {
        diLogger.error(it) { "It was not possible to load key store!" }
    }.onSuccess {
        diLogger.debug { "KeyStore loaded." }
    }.getOrThrow()

    engine {
        customizeClient {
            setSSLContext(
                SSLContextBuilder
                    .create()
                    .loadKeyMaterial(keystore, config.keyPass.toCharArray())
                    .build()
            )
        }
    }
}
