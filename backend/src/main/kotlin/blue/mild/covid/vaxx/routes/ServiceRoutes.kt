package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.DatabaseSetup
import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dto.request.SystemStatisticsFilterDtoIn
import blue.mild.covid.vaxx.dto.response.ApplicationInformationDto
import blue.mild.covid.vaxx.dto.response.ServiceHealthDtoOut
import blue.mild.covid.vaxx.dto.response.SystemStatisticsDtoOut
import blue.mild.covid.vaxx.extensions.di
import blue.mild.covid.vaxx.extensions.request
import blue.mild.covid.vaxx.extensions.respondWithStatus
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.service.SystemStatisticsService
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.get
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import org.kodein.di.instance

/**
 * Registers prometheus data.
 */
fun NormalOpenAPIRoute.serviceRoutes() {
    val version by di().instance<ApplicationInformationDto>()
    val systemStatisticsService by di().instance<SystemStatisticsService>()

    /**
     * Send data about version.
     */
    route(Routes.version).get<Unit, ApplicationInformationDto>(
        info("Returns version of the application.")
    ) { respond(version) }

    route(Routes.status).get<Unit, Unit> { respondWithStatus(HttpStatusCode.OK) }

    route(Routes.statusHealth).get<Unit, ServiceHealthDtoOut> {
        if (DatabaseSetup.isConnected()) {
            respond(ServiceHealthDtoOut("healthy"))
        } else {
            request.call.respond(HttpStatusCode.ServiceUnavailable, ServiceHealthDtoOut("DB connection is not working"))
        }
    }

    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN)) {
        route(Routes.systemStatistics).get<SystemStatisticsFilterDtoIn, SystemStatisticsDtoOut, UserPrincipal> { query ->
            respond(systemStatisticsService.getSystemStatistics(query))
        }
    }
}
