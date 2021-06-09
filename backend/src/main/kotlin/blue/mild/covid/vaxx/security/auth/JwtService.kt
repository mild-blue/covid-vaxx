package blue.mild.covid.vaxx.security.auth

import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dto.config.JwtConfigurationDto
import blue.mild.covid.vaxx.dto.response.UserLoginResponseDtoOut
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.auth.jwt.JWTCredential
import io.ktor.http.toHttpDateString
import mu.KLogging
import pw.forst.katlib.applyIf
import pw.forst.katlib.toUuid
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

/**
 * Service for issuing JWTs.
 */
class JwtService(
    private val algorithm: Algorithm,
    private val jwtConfiguration: JwtConfigurationDto
) {

    private companion object : KLogging() {
        const val ROLE = "role"
        const val NURSE = "nurse"
        const val VACCINATION_SERIAL_NUMBER = "vaxxserial"
        const val VACCINATION_EXPIRATION = "vaxxexpir"
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
            .withExpiresAt(obtainExpirationDate())
            .withIssuedAt(Date())
            .withSubject(principal.userId.toString())
            .withClaim(ROLE, principal.userRole.name)
            .withClaim(VACCINATION_SERIAL_NUMBER, principal.vaccineSerialNumber.trim())
            .withClaim(VACCINATION_EXPIRATION, principal.vaccineExpiration.format(DateTimeFormatter.ISO_LOCAL_DATE))
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
                    vaccineSerialNumber = claims.getValue(VACCINATION_SERIAL_NUMBER).asString(),
                    vaccineExpiration = LocalDate.parse(
                        claims.getValue(VACCINATION_EXPIRATION).asString(),
                        DateTimeFormatter.ISO_LOCAL_DATE
                    )
                )
            }
        }.getOrElse { throw InvalidJwtException("Invalid JWT!") }

    @Suppress("MagicNumber") // minutes to milliseconds
    private fun obtainExpirationDate(): Date =
        Date(System.currentTimeMillis() + jwtConfiguration.jwtExpirationInMinutes * 60_000)
}
