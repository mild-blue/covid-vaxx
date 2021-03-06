package blue.mild.covid.vaxx

import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.extensions.di
import blue.mild.covid.vaxx.service.EmailUserAfterRegistrationService
import blue.mild.covid.vaxx.setup.init
import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.kodein.di.instance


fun main() {



    embeddedServer(Netty, 8080, module = Application::init).start(wait = true)
}
