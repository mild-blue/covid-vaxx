package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dto.response.QuestionDtoOut
import blue.mild.covid.vaxx.extensions.di
import blue.mild.covid.vaxx.service.QuestionService
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.kodein.di.instance

/**
 * Routes related to the Question entity.
 */
fun NormalOpenAPIRoute.questionRoutes() {
    val service by di().instance<QuestionService>()

    // TODO consider allowing fetching behind auth
    // TODO #70 once ready, put behind captcha auth
    route(Routes.question) {
        get<Unit, List<QuestionDtoOut>>(
            info("Returns all questions that patient needs to answer.")
        ) {
            respond(service.getAllQuestions())
        }
    }
}
