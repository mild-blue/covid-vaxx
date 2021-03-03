package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dto.QuestionDtoOut
import blue.mild.covid.vaxx.service.QuestionService
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.kodein.di.LazyDI
import org.kodein.di.instance

fun NormalOpenAPIRoute.questionRoutes(di: LazyDI) {
    val service by di.instance<QuestionService>()

    route(Routes.question) {
        get<Unit, List<QuestionDtoOut>> {
            respond(service.getAllQuestions())
        }
    }
}
