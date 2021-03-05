package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.DatabaseSetup
import blue.mild.covid.vaxx.dto.response.ServiceHealthDtoOut
import blue.mild.covid.vaxx.dto.response.VersionDtoOut
import blue.mild.covid.vaxx.extensions.di
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.kodein.di.instance

/**
 * Registers prometheus data.
 */
fun NormalOpenAPIRoute.serviceRoutes() {
    val version by di().instance<VersionDtoOut>()

    /**
     * Send data about version.
     */
    route(Routes.version).get<Unit, VersionDtoOut>(
        info("Returns version of the application.")
    ) { respond(version) }

    route(Routes.status).get<Unit, Unit> { respond(Unit) }

    route(Routes.statusHealth).get<Unit, ServiceHealthDtoOut> {
        if (DatabaseSetup.isConnected()) {
            respond(ServiceHealthDtoOut("healthy"))
        } else {
            // TODO solve how to use different response code
            // https://github.com/papsign/Ktor-OpenAPI-Generator/wiki/A-few-examples
            respond(ServiceHealthDtoOut("DB connection is not working"))
        }
    }
}
