package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dto.request.LocationDtoIn
import blue.mild.covid.vaxx.dto.request.query.LocationIdDtoIn
import blue.mild.covid.vaxx.dto.response.EntityIdDtoOut
import blue.mild.covid.vaxx.dto.response.LocationDtoOut
import blue.mild.covid.vaxx.extensions.closestDI
import blue.mild.covid.vaxx.extensions.createLogger
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.service.LocationService
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.post
import com.papsign.ktor.openapigen.route.path.auth.principal
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.kodein.di.instance

/**
 * Routes associated with the user logins and authorizations.
 */
fun NormalOpenAPIRoute.locationRoutes() {
    val locationService by closestDI().instance<LocationService>()

    val logger = createLogger("LocationRoutes")

    route(Routes.publicLocations) {
        get<Unit, List<LocationDtoOut>>(
            info("Get all locations.")
        ) {
            respond(locationService.getAllLocations())
        }

        get<LocationIdDtoIn, LocationDtoOut>(
            info("Get location detail by given location id.")
        ) { (locationId) ->
            respond(locationService.getLocationById(locationId))
        }
    }

    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN)) {
        route(Routes.locations) {
            post<Unit, EntityIdDtoOut, LocationDtoIn, UserPrincipal>(
                info("Add new location into the system.")
            ) { _, location ->
                val principal = principal()
                logger.info {
                    "Adding location ${location.address}, ${location.zipCode} registered by ${principal.userId}."
                }
                respond(EntityIdDtoOut(locationService.addLocation(location)))
            }
        }
    }
}
