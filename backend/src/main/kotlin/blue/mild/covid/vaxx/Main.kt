package blue.mild.covid.vaxx

import blue.mild.covid.vaxx.setup.EnvVariables
import blue.mild.covid.vaxx.setup.init
import dev.forst.katlib.getEnv
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty


fun main() {
    val port = (getEnv(EnvVariables.PORT.name) ?: "8080").toInt()
    embeddedServer(Netty, port, module = Application::init).start(wait = true)
}
