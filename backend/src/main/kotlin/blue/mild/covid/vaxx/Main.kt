package blue.mild.covid.vaxx

import blue.mild.covid.vaxx.setup.init
import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty


fun main() {
    embeddedServer(Netty, 8080, module = Application::init).start(wait = true)
}
