package blue.mild.covid.vaxx.security.auth

import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dto.config.JwtConfigurationDto
import blue.mild.covid.vaxx.dto.response.UserLoginResponseDtoOut
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.auth.jwt.JWTCredential
import mu.KLogging
import pw.forst.tools.katlib.TimeProvider
import pw.forst.tools.katlib.applyIf
import pw.forst.tools.katlib.toUuid
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

/**
 * Service for issuing JWTs.
 */
class JwtService(
    private val algorithm: Algorithm,
    private val jwtConfiguration: JwtConfigurationDto,
    private val nowProvider: TimeProvider<Instant>
) {

    private companion object : KLogging() {
        const val ROLE = "role"
        const val NURSE = "nurse"
        const val VACCINATION_SERIAL_NUMBER = "vaxxserial"
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
    fun generateToken(principal: UserPrincipal): UserLoginResponseDtoOut =
        JWT.create()
            .withIssuer(jwtConfiguration.issuer)
            .withAudience(jwtConfiguration.audience)
            .withIssuedAt(Date())
            .withExpiresAt(obtainExpirationDate())
            .withSubject(principal.userId.toString())
            .withClaim(ROLE, principal.userRole.name)
            .withClaim(VACCINATION_SERIAL_NUMBER, principal.vaccineSerialNumber.trim())
            .applyIf(principal.nurseId != null) {
                withClaim(NURSE, principal.nurseId.toString())
            }
            .sign(algorithm).let {
                UserLoginResponseDtoOut(it, principal.userRole)
            }

    /**
     * Create principal from the JWT.
     */
    fun principalFromToken(credential: JWTCredential): UserPrincipal =
        runCatching {
            with(credential.payload) {
                UserPrincipal(
                    userId = subject.toUuid(),
                    userRole = UserRole.valueOf(claims.getValue(ROLE).asString()),
                    nurseId = claims[NURSE]?.takeIf { !it.isNull }?.asString()?.toUuid(),
                    vaccineSerialNumber = claims.getValue(VACCINATION_SERIAL_NUMBER).asString()
                )
            }
        }.getOrElse { throw InvalidJwtException("Invalid JWT!") }

    /**
     * We issue tokens that are valid to next day at 4AM.
     *
     * That is because of the vaccination serial number and selected nurses during the login.
     * We presume that in the beginning of the day, doctors will need to set another
     * serial number of the vaccine and maybe another nurse.
     *
     * 4AM it is because some people might work over midnight, so to be safe we made it 4AM.
     */
    @Suppress("MagicNumber")
    private fun obtainExpirationDate(): Date =
        nowProvider.now()
            .plus(1, ChronoUnit.DAYS) // add one day
            .truncatedTo(ChronoUnit.DAYS) // make it midnight of that day
            .plus(4, ChronoUnit.HOURS) // make it 4AM of UTC
            .toEpochMilli() // get the epoch time
            .let { Date(it) } // and make it to old format that the JWT understands

}
