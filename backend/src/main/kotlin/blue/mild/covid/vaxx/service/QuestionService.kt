package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.Question
import blue.mild.covid.vaxx.dto.QuestionDtoOut
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pw.forst.tools.katlib.toUuid

class QuestionService {
    suspend fun getAllQuestions(): List<QuestionDtoOut> = newSuspendedTransaction {
        Question.selectAll().map {
            QuestionDtoOut(
                id = it[Question.id].toUuid(),
                placeholder = it[Question.placeholder],
                cs = it[Question.cs],
                eng = it[Question.eng],
            )
        }
    }
}
