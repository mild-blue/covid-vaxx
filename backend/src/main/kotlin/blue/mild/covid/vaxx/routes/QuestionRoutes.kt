package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.service.QuestionService
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.kodein.di.instance
import org.kodein.di.ktor.di

fun Routing.questionRoutes() {
    val service by di().instance<QuestionService>()

    get(Routes.question) {
        call.respond(service.getAllQuestions())
    }
}
