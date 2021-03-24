package blue.mild.covid.vaxx.security.auth

import blue.mild.covid.vaxx.dto.config.JwtConfigurationDto
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

/**
 * Register services related with JWT authorization and authentication..
 */
fun DI.MainBuilder.registerJwtAuth() {

    bind<Algorithm>() with singleton { Algorithm.HMAC256(instance<JwtConfigurationDto>().signingSecret) }

    bind<JwtAuthProvider>() with singleton { JwtAuthProvider() }

    bind<JwtService>() with singleton { JwtService(instance(), instance(), instance()) }

    bind<JWTVerifier>() with singleton { instance<JwtService>().makeJwtVerifier() }
}
