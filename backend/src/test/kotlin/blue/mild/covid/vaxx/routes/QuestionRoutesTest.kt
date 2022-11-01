package blue.mild.covid.vaxx.routes

import blue.mild.covid.vaxx.dao.model.Questions
import blue.mild.covid.vaxx.dto.response.QuestionDtoOut
import blue.mild.covid.vaxx.service.QuestionService
import blue.mild.covid.vaxx.utils.ServerTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.kodein.di.instance
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class QuestionRoutesTest : ServerTestBase() {
    @Test
    fun `server should return all questions from the database`() = withTestApplication {
        val questionService by closestDI().instance<QuestionService>()

        val allQuestions = runBlocking { questionService.getCachedQuestions() }
        handleRequest(HttpMethod.Get, Routes.questions).run {
            expectStatus(HttpStatusCode.OK)
            assertEquals(allQuestions, receive())
        }
    }

    @Test
    fun `cache refresh should update questions cache`() = withTestApplication {
        val questionService by closestDI().instance<QuestionService>()
        // these are all question currently in the cache, that is used to serve all questions
        // at this point of time in this particular test, the cache returns all questions
        // from the database -- the cache and database are in sync
        val allQuestions = runBlocking { questionService.getCachedQuestions() }

        // now we break the synchronization between the cache and the database
        val newQuestionId = transaction {
            Questions.insert {
                it[placeholder] = "mood"
                it[cs] = "Jak se mas?"
                it[eng] = "How are you doing?"
            }[Questions.id]
        }
        // verify that the cache is still the same - the endpoint questions would
        // return exactly the same data as the cache, we're not calling it here,
        // as that is tested in the previous test
        assertEquals(allQuestions, runBlocking { questionService.getCachedQuestions() })
        assertTrue { allQuestions.none { it.id == newQuestionId } }

        // verify that this is authorized endpoint
        handleRequest(HttpMethod.Get, Routes.cacheRefresh).run {
            expectStatus(HttpStatusCode.Unauthorized)
        }

        // now we force server to reload the cache
        handleRequest(HttpMethod.Get, Routes.cacheRefresh) { authorize() }.run {
            expectStatus(HttpStatusCode.OK)
            val refreshedCache = receive<List<QuestionDtoOut>>()
            // check that the returned data are not the one from the previous cache
            assertNotEquals(allQuestions, refreshedCache)
            // and that they contain the new question
            assertTrue { refreshedCache.any { it.id == newQuestionId } }
        }
    }
}
