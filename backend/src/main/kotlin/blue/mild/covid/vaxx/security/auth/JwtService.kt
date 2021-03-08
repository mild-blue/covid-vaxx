package blue.mild.covid.vaxx.security.auth

import blue.mild.covid.vaxx.dao.UserRole
import blue.mild.covid.vaxx.dto.config.JwtConfigurationDto
import blue.mild.covid.vaxx.dto.response.BearerTokenDtoOut
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.auth.jwt.JWTCredential
import mu.KLogging
import pw.forst.tools.katlib.toUuid
import java.util.Date

/**
 * Service for issuing JWTs.
 */
class JwtService(
    private val algorithm: Algorithm,
    private val jwtConfiguration: JwtConfigurationDto
) {

    private companion object : KLogging() {
        const val PUBLIC_SUBJECT = "public"
        const val ROLE = "role"
        const val TYPE = "type"
    }

    /**
     * Build JWT verifier for JWTs issued by this service.
     */
    fun makeJwtVerifier(): JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(jwtConfiguration.issuer)
        .withAudience(jwtConfiguration.audience)
        .build()

    /**
     * Generate and sign JWT for given principal.¬
     */
    fun generateToken(principal: UserPrincipal): BearerTokenDtoOut {
        val builder = JWT.create()
            .withIssuer(jwtConfiguration.issuer)
            .withAudience(jwtConfiguration.audience)
            .withExpiresAt(obtainExpirationDate(principal))
            .withIssuedAt(Date())
            .withClaim(TYPE, nameOf(principal))

        return when (principal) {
            is PatientPrincipal -> builder.forPatientRegistration()
            is RegisteredUserPrincipal -> builder.forRegisteredUser(principal)
        }.sign(algorithm).let(::BearerTokenDtoOut)
    }

    private fun JWTCreator.Builder.forPatientRegistration() = withSubject(PUBLIC_SUBJECT)

    private fun JWTCreator.Builder.forRegisteredUser(principal: RegisteredUserPrincipal) =
        this.withSubject(principal.userId.toString())
            .withClaim(ROLE, principal.userRole.name)

    /**
     * Create principal from the JWT.
     */
    fun principalFromToken(credential: JWTCredential): UserPrincipal =
        runCatching {
            with(credential.payload) {
                when (claims.getValue(TYPE).asString()) {
                    nameOf<PatientPrincipal>() -> PatientPrincipal
                    nameOf<RegisteredUserPrincipal>() -> RegisteredUserPrincipal(
                        userId = subject.toUuid(),
                        userRole = UserRole.valueOf(claims.getValue(ROLE).asString())
                    )
                    else -> throw InvalidJwtException("Invalid JWT!")
                }
            }
        }.getOrElse { throw InvalidJwtException("Invalid JWT!") }

    private fun obtainExpirationDate(principal: UserPrincipal): Date =
        when (principal) {
            is PatientPrincipal -> Date(System.currentTimeMillis() + jwtConfiguration.patientUserJwtExpirationInMinutes * 60_000)
            is RegisteredUserPrincipal -> Date(System.currentTimeMillis() + jwtConfiguration.registeredUserJwtExpirationInMinutes * 60_000)
        }

    // as we're using just sealed classes, we can require not null here
    private inline fun <reified T : Any> nameOf(): String = requireNotNull(T::class.simpleName)
    private inline fun <reified T : Any> nameOf(obj: T): String = requireNotNull(obj::class.simpleName)
}
