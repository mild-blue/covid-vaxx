package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dao.model.VaccinationSlots.locationId
import blue.mild.covid.vaxx.dto.request.CreateVaccinationSlotsDtoIn
import blue.mild.covid.vaxx.dto.request.LocationDtoIn
import blue.mild.covid.vaxx.dto.request.query.LocationIdDtoIn
import blue.mild.covid.vaxx.dto.response.LocationDtoOut
import blue.mild.covid.vaxx.dto.response.OK
import blue.mild.covid.vaxx.dto.response.Ok
import blue.mild.covid.vaxx.extensions.determineRealIp
import blue.mild.covid.vaxx.extensions.di
import blue.mild.covid.vaxx.extensions.request
import blue.mild.covid.vaxx.extensions.respondWithStatus
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.service.LocationService
import blue.mild.covid.vaxx.utils.createLogger
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.get
import com.papsign.ktor.openapigen.route.path.auth.post
import com.papsign.ktor.openapigen.route.path.auth.principal
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import io.ktor.http.HttpStatusCode
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
        }
    }

    authorizeRoute {
        route(Routes.locationsSlots) {
            get<LocationIdDtoIn, Unit, UserPrincipal>(
                info("Verify that the currently used token is valid. Returns 200 if token is correct, 401 otherwise.")
            ) { (locationId) ->
                logger.info {
                    "Get slots for location ${locationId} from host ${request.determineRealIp()}."
                }
                respondWithStatus(HttpStatusCode.OK)
            }

            /*
            // MartinLlama - When tests are executed then when calling POST .../locations/{id}/slots returns 404
            post<LocationIdDtoIn, Ok, /*EntityId, */CreateVaccinationSlotsDtoIn, UserPrincipal>(
                info("Add new slots for location.")
            ) { locationId, createSlots ->
                val principal = principal()
                logger.info {
                    "For location ${locationId} adding slots ${createSlots} registered by ${principal.userId} from host ${request.determineRealIp()}."
                }
                // respond(locationService.addLocation(location))
                respond(OK)
            }
            */

            // MartinLLama - When tests are executed then locationId is not extracted
            post<Unit, Ok, /*EntityId, */CreateVaccinationSlotsDtoIn, UserPrincipal>(
                info("Add new slots for location.")
            ) { _, createSlots ->
                val principal = principal()
                logger.info {
                    "For location ${locationId} adding slots ${createSlots} registered by ${principal.userId} from host ${request.determineRealIp()}."
                }
                // respond(locationService.addLocation(location))
                respond(OK)
            }
        }
    }
}
