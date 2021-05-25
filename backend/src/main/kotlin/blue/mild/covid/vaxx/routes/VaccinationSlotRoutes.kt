package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dto.request.CreateVaccinationSlotsDtoIn
import blue.mild.covid.vaxx.dto.request.PatientVaccinationSlotSelectionDtoIn
import blue.mild.covid.vaxx.dto.request.query.MultipleVaccinationSlotsQueryDtoOut
import blue.mild.covid.vaxx.dto.response.VaccinationSlotDtoOut
import blue.mild.covid.vaxx.extensions.determineRealIp
import blue.mild.covid.vaxx.extensions.di
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
    val vaccinationSlotService by di().instance<VaccinationSlotService>()

    val logger = createLogger("LocationRoutes")

    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN)) {
        route(Routes.vaccinationSlots) {
            post<Unit, List<EntityId>, CreateVaccinationSlotsDtoIn, UserPrincipal>(
                info("Add new vaccination slots for given location.")
            ) { location, createSlots ->
                val principal = principal()
                logger.info {
                    "For location ${location} adding slots ${createSlots} by ${principal.userId} from host ${request.determineRealIp()}."
                }
                respond(vaccinationSlotService.addSlots(createSlots))
            }

            route("filter") {
                get<MultipleVaccinationSlotsQueryDtoOut, List<VaccinationSlotDtoOut>, UserPrincipal>(
                    info("Get vaccination spots matching ")
                ) { slotsQuery ->
                    val principal = principal()
                    logger.info { "User ${principal.userId} search query: $slotsQuery." }

                    val slots = vaccinationSlotService.getSlotsByConjunctionOf(
                        id = slotsQuery.id,
                        locationId = slotsQuery.locationId,
                        fromMillis = slotsQuery?.fromMillis,
                        toMillis = slotsQuery?.toMillis,
                        status = slotsQuery.status,
                    )

                    logger.info { "Found ${slots.size} records." }
                    logger.debug { "Returning slots: ${slots.joinToString(", ") { it.id.toString() }}." }

                    respond(slots)
                }

                post<MultipleVaccinationSlotsQueryDtoOut, VaccinationSlotDtoOut, PatientVaccinationSlotSelectionDtoIn, UserPrincipal>(
                    info("Get patient the parameters. Filters by and clause. Empty parameters return all patients.")
                ) { slotsQuery, patientDtoIn ->
                    val principal = principal()
                    logger.info { "User ${principal.userId} reserves query: $slotsQuery for ${patientDtoIn}."}

                    val slot = vaccinationSlotService.updateSlot(
                        id = slotsQuery.id,
                        locationId = slotsQuery.locationId,
                        fromMillis = slotsQuery?.fromMillis,
                        toMillis = slotsQuery?.toMillis,
                        status = slotsQuery.status,
                        patientId = patientDtoIn.patientId,
                    )

                    logger.info { "Updated ${slot} record." }

                    respond(slot)
                }
            }
        }
    }
}
