package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.DatabaseSetup
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.kodein.di.LazyDI
import org.kodein.di.instance

/**
 * Registers prometheus data.
 */
fun NormalOpenAPIRoute.serviceRoutes(di: LazyDI) {
    val version by di.instance<String>("version")

    /**
     * Send data about version.
     */
    route(Routes.version).get<Unit, VersionDtoOut> { respond(VersionDtoOut(version)) }

    route(Routes.status).get<Unit, Unit> { respond(Unit) }

    route(Routes.statusHealth).get<Unit, HealthDtoOut> {
        if (DatabaseSetup.isConnected()) {
            respond(HealthDtoOut("healthy"))
        } else {
            // TODO solve how to use different response code
            // https://github.com/papsign/Ktor-OpenAPI-Generator/wiki/A-few-examples
            respond(HealthDtoOut("DB connection is not working"))
        }

    }
}

data class VersionDtoOut(val version: String)
data class HealthDtoOut(val health: String)
