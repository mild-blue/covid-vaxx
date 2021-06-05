package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dto.request.CreateVaccinationSlotsDtoIn
import blue.mild.covid.vaxx.dto.request.query.MultipleVaccinationSlotsQueryDtoIn
import blue.mild.covid.vaxx.dto.response.VaccinationSlotDtoOut
import blue.mild.covid.vaxx.extensions.closestDI
import blue.mild.covid.vaxx.extensions.determineRealIp
import blue.mild.covid.vaxx.extensions.request
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.service.VaccinationSlotService
import blue.mild.covid.vaxx.utils.createLogger
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.get
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
    val vaccinationSlotService by closestDI().instance<VaccinationSlotService>()

    val logger = createLogger("LocationRoutes")

    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN)) {
        route(Routes.vaccinationSlots).post<Unit, List<EntityId>, CreateVaccinationSlotsDtoIn, UserPrincipal>(
            info("Generate new vaccination slots for given location.")
        ) { _, createSlots ->
            val principal = principal()
            logger.info {
                "For location ${createSlots.locationId} adding slots by ${principal.userId} from host ${request.determineRealIp()}."
            }
            val slots = vaccinationSlotService.addSlots(createSlots)
            logger.info { "${slots.size} vaccination slots created." }

            respond(slots)
        }
    }

    authorizeRoute {
        route(Routes.vaccinationSlots) {
            route("filter").get<MultipleVaccinationSlotsQueryDtoIn, List<VaccinationSlotDtoOut>, UserPrincipal>(
                info("Get vaccination spots matching ")
            ) { slotsQuery ->
                val principal = principal()
                logger.info { "User ${principal.userId} search query: $slotsQuery." }

                val slots = vaccinationSlotService.getSlotsByConjunctionOf(
                    slotId = slotsQuery.id,
                    locationId = slotsQuery.locationId,
                    patientId = slotsQuery.patientId,
                    from = slotsQuery.from,
                    to = slotsQuery.to,
                    status = slotsQuery.status,
                )

                logger.info { "Found ${slots.size} records." }

                respond(slots)
            }
        }
    }
}
