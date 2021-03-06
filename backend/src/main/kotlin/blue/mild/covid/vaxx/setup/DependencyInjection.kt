package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.service.*
import blue.mild.covid.vaxx.service.EmailUserAfterRegistrationService
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

fun DI.MainBuilder.registerClasses() {
    bind<EntityIdProvider>() with singleton { EntityIdProvider() }
    bind<InstantTimeProvider>() with singleton { InstantTimeProvider() }
    bind<PasswordHashProvider>() with singleton { PasswordHashProvider() }
    bind<QuestionService>() with singleton { QuestionService() }
    bind<ValidationService>() with singleton { ValidationService(instance()) }
    bind<PatientService>() with singleton { PatientService(instance(), instance(), instance()) }
    bind<UserService>() with singleton { UserService(instance()) }
    bind<EmailUserAfterRegistrationService>() with singleton { EmailUserAfterRegistrationService(instance()) }
}
