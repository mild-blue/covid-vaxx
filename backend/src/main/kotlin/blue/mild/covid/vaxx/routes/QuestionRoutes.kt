package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dto.response.QuestionDtoOut
import blue.mild.covid.vaxx.extensions.closestDI
import blue.mild.covid.vaxx.security.auth.UserPrincipal
import blue.mild.covid.vaxx.security.auth.authorizeRoute
import blue.mild.covid.vaxx.service.QuestionService
import blue.mild.covid.vaxx.utils.createLogger
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.auth.get
import com.papsign.ktor.openapigen.route.path.auth.principal
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.kodein.di.instance

/**
 * Routes related to the Question entity.
 */
fun NormalOpenAPIRoute.questionRoutes() {
    val logger = createLogger("QuestionRoutes")

    val questionService by closestDI().instance<QuestionService>()

    route(Routes.questions) {
        get<Unit, List<QuestionDtoOut>>(
            info("Returns all questions that patient needs to answer.")
        ) {
            respond(questionService.getCachedQuestions())
        }
    }

    authorizeRoute(requireOneOf = setOf(UserRole.ADMIN)) {
        route(Routes.cacheRefresh) {
            get<Unit, List<QuestionDtoOut>, UserPrincipal>(
                info("Returns all questions and refreshes in-memory cache.")
            ) {
                val principal = principal()
                logger.info { "Cache refresh request administrated by ${principal.userId}." }
                respond(questionService.refreshCache())
            }
        }
    }
}
