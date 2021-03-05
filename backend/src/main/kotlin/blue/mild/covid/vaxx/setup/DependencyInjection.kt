package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.service.EntityIdProvider
import blue.mild.covid.vaxx.service.InstantTimeProvider
import blue.mild.covid.vaxx.service.PatientService
import blue.mild.covid.vaxx.service.QuestionService
import blue.mild.covid.vaxx.service.UserLoginService
import blue.mild.covid.vaxx.service.ValidationService
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

fun DI.MainBuilder.registerClasses() {
    bind<EntityIdProvider>() with singleton { EntityIdProvider() }
    bind<InstantTimeProvider>() with singleton { InstantTimeProvider() }
    bind<QuestionService>() with singleton { QuestionService() }
    bind<ValidationService>() with singleton { ValidationService(instance()) }
    bind<PatientService>() with singleton { PatientService(instance(), instance(), instance()) }
    bind<UserLoginService>() with singleton { UserLoginService() }
}
