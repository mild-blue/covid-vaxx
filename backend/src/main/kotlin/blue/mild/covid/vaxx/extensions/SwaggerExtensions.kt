package blue.mild.covid.vaxx.extensions

import blue.mild.covid.vaxx.dto.internal.ContextAware
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import com.papsign.ktor.openapigen.route.OpenAPIRoute
import com.papsign.ktor.openapigen.route.path.auth.principal
import com.papsign.ktor.openapigen.route.response.OpenAPIPipelineAuthContext
import com.papsign.ktor.openapigen.route.response.OpenAPIPipelineResponseContext
import io.ktor.features.callId
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import org.kodein.di.ktor.closestDI

/**
 * Simple extension to provide DI container inside Swagger defined routes.
 */
fun OpenAPIRoute<*>.closestDI() = ktorRoute.closestDI()

val OpenAPIPipelineResponseContext<*>.context
    get() = pipeline.context


val OpenAPIPipelineResponseContext<*>.request
    get() = context.request

suspend fun OpenAPIPipelineResponseContext<*>.respondWithStatus(status: HttpStatusCode) = request.call.respond(status)

/**
 * Creates [ContextAware.PublicContext] from current pipeline and given [payload].
 */
fun <TPayload, TResponse> OpenAPIPipelineResponseContext<TResponse>.asContextAware(payload: TPayload) =
    ContextAware.PublicContext(
        remoteHost = request.determineRealIp(),
        callId = request.call.callId,
        payload = payload,
    )

/**
 * Creates [ContextAware.AuthorizedContext] from current pipeline and given [payload].
 */
suspend fun <TPayload, TResponse> OpenAPIPipelineAuthContext<UserPrincipal, TResponse>.asContextAware(payload: TPayload) =
    ContextAware.AuthorizedContext(
        remoteHost = request.determineRealIp(),
        callId = request.call.callId,
        payload = payload,
        principal = principal()
    )
