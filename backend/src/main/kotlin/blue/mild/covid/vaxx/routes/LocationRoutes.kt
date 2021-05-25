package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dto.request.LocationDtoIn
import blue.mild.covid.vaxx.dto.request.query.LocationIdDtoIn
import blue.mild.covid.vaxx.dto.response.LocationDtoOut
import blue.mild.covid.vaxx.extensions.determineRealIp
import blue.mild.covid.vaxx.extensions.di
import blue.mild.covid.vaxx.extensions.request
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.service.LocationService
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
fun NormalOpenAPIRoute.locationRoutes() {
    val locationService by di().instance<LocationService>()

    val logger = createLogger("LocationRoutes")

    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN)) {
        route(Routes.locations) {
            post<Unit, EntityId, LocationDtoIn, UserPrincipal>(
                info("Add new location into the system.")
            ) { _, location ->
                val principal = principal()
                logger.info {
                    "Adding location ${location.address}, ${location.zipCode} registered by ${principal.userId} from host ${request.determineRealIp()}."
                }
                respond(locationService.addLocation(location))
            }

            get<LocationIdDtoIn, LocationDtoOut, UserPrincipal>(
                info("Get location detail by given location id.")
            ) { (locationId) ->
                respond(locationService.getLocationById(locationId))
            }

//            // MartinLLama - Locations: I am trying to make endpoint /locations/{id}/slots - this does not work
//            // https://ktor.io/docs/routing-in-ktor.html#multiple_routes - based on sub-routes
//            route("/slots") {
//                post<LocationIdDtoIn, List<EntityId>, CreateVaccinationSlotsDtoIn, UserPrincipal>(
//                    info("Add new vaccination slots into the system.")
//                ) { location, createSlots ->
//                    val principal = principal()
//                    logger.info {
//                        "For location ${location} adding slots ${createSlots} by ${principal.userId} from host ${request.determineRealIp()}."
//                    }
//                    respond(vaccinationSlotService.addSlots(createSlots, location.id))
//                }
//            }
        }

// MartinLLama - Locations: I am trying to make endpoint /locations/{id}/slots - this does not work
//        route(Routes.locationsSlots) {
//            post<LocationIdDtoIn, List<EntityId>, CreateVaccinationSlotsDtoIn, UserPrincipal>(
//                info("Add new vaccination slots into the system.")
//            ) { location, createSlots ->
//                val principal = principal()
//                logger.info {
//                    "For location ${location} adding slots ${createSlots} by ${principal.userId} from host ${request.determineRealIp()}."
//                }
//                respond(vaccinationSlotService.addSlots(createSlots, location.id))
//            }
//        }
    }
}
