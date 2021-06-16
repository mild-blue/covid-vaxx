package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.DatabaseSetup
import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dto.request.IsinJobDtoIn
import blue.mild.covid.vaxx.dto.request.query.SystemStatisticsFilterDtoIn
import blue.mild.covid.vaxx.dto.response.ApplicationInformationDtoOut
import blue.mild.covid.vaxx.dto.response.IsinJobDtoOut
import blue.mild.covid.vaxx.dto.response.ServiceHealthDtoOut
import blue.mild.covid.vaxx.dto.response.SystemStatisticsDtoOut
import blue.mild.covid.vaxx.extensions.closestDI
import blue.mild.covid.vaxx.extensions.createLogger
import blue.mild.covid.vaxx.extensions.determineRealIp
import blue.mild.covid.vaxx.extensions.request
import blue.mild.covid.vaxx.extensions.respondWithStatus
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.service.IsinRetryService
import blue.mild.covid.vaxx.service.SystemStatisticsService
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.post
import com.papsign.ktor.openapigen.route.path.auth.principal
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
    val version by closestDI().instance<ApplicationInformationDtoOut>()
    val systemStatisticsService by closestDI().instance<SystemStatisticsService>()
    val isinRetryService by closestDI().instance<IsinRetryService>()

    val logger = createLogger("ServiceRoute")

    /**
     * Send data about version.
     */
    route(Routes.version).get<Unit, ApplicationInformationDtoOut>(
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

    route(Routes.systemStatistics).get<SystemStatisticsFilterDtoIn, SystemStatisticsDtoOut> { query ->
        respond(systemStatisticsService.getSystemStatistics(query))
    }

    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN)) {
        route(Routes.runIsinJob).post<Unit, IsinJobDtoOut, IsinJobDtoIn, UserPrincipal>(
            info(
                "Checks patients where ISIN has failed and run ISIN client again."
            )
        ) { _, isinJobDto ->
            val principal = principal()
            logger.info {
                "Run ISIN job by ${principal.userId} from host ${request.determineRealIp()}."
            }

            val stats = isinRetryService.runIsinRetry(isinJobDto)

            respond(stats)
        }
    }
}
