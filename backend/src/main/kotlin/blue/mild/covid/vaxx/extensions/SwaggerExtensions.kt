package blue.mild.covid.vaxx.extensions

import com.papsign.ktor.openapigen.route.OpenAPIRoute
import org.kodein.di.ktor.di

/**
 * Simple extension to provide DI container inside Swagger defined routes.
 */
fun OpenAPIRoute<*>.di() = ktorRoute.di()

