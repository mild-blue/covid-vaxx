package blue.mild.covid.vaxx.extensions

import com.papsign.ktor.openapigen.route.OpenAPIRoute
import com.papsign.ktor.openapigen.route.response.OpenAPIPipelineResponseContext
import org.kodein.di.ktor.di

/**
 * Simple extension to provide DI container inside Swagger defined routes.
 */
fun OpenAPIRoute<*>.di() = ktorRoute.di()

val OpenAPIPipelineResponseContext<*>.request
    get() = pipeline.context.request

val OpenAPIPipelineResponseContext<*>.context
    get() = pipeline.context
