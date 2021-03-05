package blue.mild.covid.vaxx.extensions

import com.papsign.ktor.openapigen.route.OpenAPIRoute
import com.papsign.ktor.openapigen.route.response.OpenAPIPipelineResponseContext
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import org.kodein.di.ktor.di

/**
 * Simple extension to provide DI container inside Swagger defined routes.
 */
fun OpenAPIRoute<*>.di() = ktorRoute.di()

val OpenAPIPipelineResponseContext<*>.context
    get() = pipeline.context


val OpenAPIPipelineResponseContext<*>.request
    get() = context.request

suspend fun OpenAPIPipelineResponseContext<*>.respond(status: HttpStatusCode) = request.call.respond(status)
