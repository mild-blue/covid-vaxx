package blue.mild.covid.vaxx.auth

import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

fun DI.MainBuilder.registerAuth() {

    bind<Algorithm>() with singleton { Algorithm.HMAC256(instance<JwtConfigurationDto>().signingSecret) }

    bind<JwtAuthProvider>() with singleton { JwtAuthProvider() }

    bind<JwtService>() with singleton { JwtService(instance(), instance()) }

    bind<JWTVerifier>() with singleton { instance<JwtService>().makeJwtVerifier() }
}
