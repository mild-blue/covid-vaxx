package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dto.request.CreateVaccinationSlotsDtoIn
import blue.mild.covid.vaxx.extensions.determineRealIp
import blue.mild.covid.vaxx.extensions.di
import blue.mild.covid.vaxx.extensions.request
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.service.VaccinationSlotService
import blue.mild.covid.vaxx.utils.createLogger
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.post
import com.papsign.ktor.openapigen.route.path.auth.principal
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.kodein.di.instance

/**
 * Routes associated with the user logins and authorizations.
 */
fun NormalOpenAPIRoute.vaccinationSlotRoutes() {
    val vaccinationSlotService by di().instance<VaccinationSlotService>()

    val logger = createLogger("LocationRoutes")

    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN)) {
        route(Routes.vaccinationSlots) {
            post<Unit, List<EntityId>, CreateVaccinationSlotsDtoIn, UserPrincipal>(
                info("Add new location into the system.")
            ) { location, createSlots ->
                val principal = principal()
                logger.info {
                    "For location ${location} adding slots ${createSlots} by ${principal.userId} from host ${request.determineRealIp()}."
                }
                respond(vaccinationSlotService.addSlots(createSlots))
            }
        }
    }
}
