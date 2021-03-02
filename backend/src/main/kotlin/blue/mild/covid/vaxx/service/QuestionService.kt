package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.Question
import blue.mild.covid.vaxx.dto.QuestionDto
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pw.forst.tools.katlib.toUuid

class QuestionService {
    suspend fun getAllQuestions(): List<QuestionDto> = newSuspendedTransaction {
        Question.selectAll().map {
            QuestionDto(
                id = it[Question.id].toUuid(),
                placeholder = it[Question.placeholder],
                cs = it[Question.cs],
                eng = it[Question.eng],
            )
        }
    }
}
