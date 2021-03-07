package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.service.EmailService
import blue.mild.covid.vaxx.service.EntityIdProvider
import blue.mild.covid.vaxx.service.InstantTimeProvider
import blue.mild.covid.vaxx.service.PasswordHashProvider
import blue.mild.covid.vaxx.service.PatientService
import blue.mild.covid.vaxx.service.QuestionService
import blue.mild.covid.vaxx.service.UserService
import blue.mild.covid.vaxx.service.ValidationService
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import pw.forst.tools.katlib.TimeProvider
import java.time.Instant

fun DI.MainBuilder.registerClasses() {
    bind<EntityIdProvider>() with singleton { EntityIdProvider() }
    bind<InstantTimeProvider>() with singleton { InstantTimeProvider() }
    bind<PasswordHashProvider>() with singleton { PasswordHashProvider() }
    bind<QuestionService>() with singleton { QuestionService() }
    bind<ValidationService>() with singleton { ValidationService(instance()) }
    bind<PatientService>() with singleton { PatientService(instance(), instance(), instance()) }
    bind<UserService>() with singleton { UserService(instance()) }
    bind<EmailService>() with singleton { EmailService(instance(), instance()) }
    bind<TimeProvider<Instant>>() with singleton { pw.forst.tools.katlib.InstantTimeProvider }
}
