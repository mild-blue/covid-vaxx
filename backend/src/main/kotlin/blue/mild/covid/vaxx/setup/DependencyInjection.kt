package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.dao.repository.PatientRepository
import blue.mild.covid.vaxx.dao.repository.UserRepository
import blue.mild.covid.vaxx.dto.config.MailJetConfigurationDto
import blue.mild.covid.vaxx.service.CaptchaVerificationService
import blue.mild.covid.vaxx.service.DummyMailService
import blue.mild.covid.vaxx.service.EntityIdProvider
import blue.mild.covid.vaxx.service.MailJetEmailService
import blue.mild.covid.vaxx.service.MailService
import blue.mild.covid.vaxx.service.PasswordHashProvider
import blue.mild.covid.vaxx.service.PatientService
import blue.mild.covid.vaxx.service.QuestionService
import blue.mild.covid.vaxx.service.UserService
import blue.mild.covid.vaxx.service.ValidationService
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
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
import freemarker.template.Configuration as FreemakerConfiguration

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
            mailJetConfig.apiKey,
            mailJetConfig.apiSecret,
            ClientOptions("v3.1")
        )
    }
    bind<DummyMailService>() with singleton { DummyMailService() }

    bind<FreemakerConfiguration>() with singleton {
        FreemakerConfiguration().apply {
            setClassForTemplateLoading(MailJetEmailService::class.java, "/templates")
        }
    }

    bind<MailService>() with singleton {
        if (instance(EnvVariables.ENABLE_MAIL_SERVICE)) {
            instance<MailJetEmailService>()
        } else {
            instance<DummyMailService>()
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

}

