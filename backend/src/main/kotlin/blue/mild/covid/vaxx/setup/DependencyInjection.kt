package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.dao.repository.PatientRepository
import blue.mild.covid.vaxx.dao.repository.UserRepository
import blue.mild.covid.vaxx.dto.config.MailJetConfigurationDto
import blue.mild.covid.vaxx.security.ddos.CaptchaVerificationService
import blue.mild.covid.vaxx.security.ddos.RequestVerificationService
import blue.mild.covid.vaxx.service.EntityIdProvider
import blue.mild.covid.vaxx.service.IsinRegistrationService
import blue.mild.covid.vaxx.service.MailJetEmailService
import blue.mild.covid.vaxx.service.MailService
import blue.mild.covid.vaxx.service.MedicalRegistrationService
import blue.mild.covid.vaxx.service.PasswordHashProvider
import blue.mild.covid.vaxx.service.PatientService
import blue.mild.covid.vaxx.service.QuestionService
import blue.mild.covid.vaxx.service.UserService
import blue.mild.covid.vaxx.service.ValidationService
import blue.mild.covid.vaxx.service.dummy.DummyMailService
import blue.mild.covid.vaxx.service.dummy.DummyMedicalRegistrationService
import blue.mild.covid.vaxx.service.dummy.DummyRequestVerificationService
import blue.mild.covid.vaxx.utils.createLogger
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
import freemarker.template.Configuration
import freemarker.template.Version
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import pw.forst.tools.katlib.InstantTimeProvider
import pw.forst.tools.katlib.TimeProvider
import java.time.Instant

/**
 * Register instances that are created only when needed.
 */
fun DI.MainBuilder.registerClasses() {
    bind<PatientRepository>() with singleton { PatientRepository(instance()) }
    bind<UserRepository>() with singleton { UserRepository() }

    bind<EntityIdProvider>() with singleton { EntityIdProvider() }
    bind<PasswordHashProvider>() with singleton { PasswordHashProvider() }
    bind<QuestionService>() with singleton { QuestionService() }
    bind<ValidationService>() with singleton { ValidationService(instance()) }
    bind<PatientService>() with singleton { PatientService(instance(), instance(), instance()) }
    bind<UserService>() with singleton { UserService(instance(), instance(), instance()) }
    bind<MailJetEmailService>() with singleton { MailJetEmailService(instance(), instance(), instance(), instance()) }
    bind<TimeProvider<Instant>>() with singleton { InstantTimeProvider }

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
        Configuration(Version(2, 3, 31)).apply {
            setClassForTemplateLoading(MailJetEmailService::class.java, "/templates")
        }
    }

    bind<HttpClient>() with singleton {
        HttpClient(Apache) {
            install(JsonFeature) {
                serializer = JacksonSerializer { registerModule(JavaTimeModule()) }
            }
        }
    }

    bind<CaptchaVerificationService>() with singleton { CaptchaVerificationService(instance(), instance()) }
    bind<DummyRequestVerificationService>() with singleton { DummyRequestVerificationService() }

    bind<IsinRegistrationService>() with singleton { IsinRegistrationService(instance(), instance(), instance()) }
    bind<DummyMedicalRegistrationService>() with singleton { DummyMedicalRegistrationService() }

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

