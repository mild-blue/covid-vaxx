package blue.mild.covid.vaxx.auth

import blue.mild.covid.vaxx.dao.UserRole
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.application.feature
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.routing.Route
import io.ktor.routing.RouteSelector
import io.ktor.routing.RouteSelectorEvaluation
import io.ktor.routing.RoutingResolveContext
import io.ktor.routing.application
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelinePhase


class RoleBasedAuthorization {

    private val interceptPhase = PipelinePhase("Authorization")

    fun interceptPipeline(
        pipeline: ApplicationCallPipeline,
        anyOf: Set<UserRole> = emptySet()
    ) = with(pipeline) {
        insertPhaseAfter(ApplicationCallPipeline.Features, Authentication.ChallengePhase)
        insertPhaseAfter(Authentication.ChallengePhase, interceptPhase)
        // todo correct exceptions
        intercept(interceptPhase) {
            when (val principal = call.authentication.principal<UserPrincipal>()) {
                is PatientPrincipal -> {
                    if (anyOf.isNotEmpty()) {
                        throw AuthorizationException("Wrong token type.")
                    }
                }
                is RegisteredUserPrincipal -> {
                    // todo maybe check for empty anyO
                    if (principal.userRole !in anyOf) {
                        throw AuthorizationException("No sufficient rights!")
                    }
                }
                null -> throw AuthorizationException("Missing principal")
            }
        }
    }


    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Unit, RoleBasedAuthorization> {
        override val key = AttributeKey<RoleBasedAuthorization>("RoleBasedAuthorization")

        override fun install(
            pipeline: ApplicationCallPipeline, configure: Unit.() -> Unit
        ): RoleBasedAuthorization = RoleBasedAuthorization()
    }
}
