package blue.mild.covid.vaxx.setup

import blue.mild.covid.vaxx.service.PatientService
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

fun DI.MainBuilder.registerClasses() {
    bind<PatientService>() with singleton { PatientService() }
}
