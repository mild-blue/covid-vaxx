package blue.mild.covid.vaxx.auth

import blue.mild.covid.vaxx.dao.UserRole
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.auth.Authentication
import io.ktor.auth.authentication
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelinePhase


/**
 * Ktor feature that allows us to implement role based authorization.
 */
class RoleBasedAuthorization {

    private val interceptPhase = PipelinePhase("Authorization")

    /**
     * Request call pipeline interception that injects role based authorization.
     * If [anyOf] is not empty, the intercept requires for at least one of the roles
     * to be present in the [RegisteredUserPrincipal].
     *
     * However, if [anyOf] is empty, the interceptor still requires at least any principals to be present.
     * Thus, all instances of [UserPrincipal] are allowed.
     */
    fun interceptPipeline(
        pipeline: ApplicationCallPipeline,
        anyOf: Set<UserRole> = emptySet()
    ) = with(pipeline) {
        // install this intercept to the authentication phase
        insertPhaseAfter(ApplicationCallPipeline.Features, Authentication.ChallengePhase)
        insertPhaseAfter(Authentication.ChallengePhase, interceptPhase)
        intercept(interceptPhase) {
            when (val principal = call.authentication.principal<UserPrincipal>()) {
                is PatientPrincipal -> {
                    if (anyOf.isNotEmpty()) {
                        throw InsufficientRightsException("You must be registered user to perform this action.")
                    }
                }
                is RegisteredUserPrincipal -> {
                    if (anyOf.isNotEmpty() && principal.userRole !in anyOf) {
                        throw InsufficientRightsException("This action requires to be one of ${anyOf.joinToString(", ")}.")
                    }
                }
                null -> throw GenericAuthException("Missing principal!")
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
