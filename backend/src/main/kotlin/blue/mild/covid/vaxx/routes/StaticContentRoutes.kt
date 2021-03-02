package blue.mild.covid.vaxx.routes

import io.ktor.http.content.default
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.routing.Routing
import org.kodein.di.instance
import org.kodein.di.ktor.di

fun Routing.staticContentRoutes() {

    val basePath by di().instance<String>("frontend")

    static {
        files(basePath)
        default("$basePath/index.html")
    }
}
