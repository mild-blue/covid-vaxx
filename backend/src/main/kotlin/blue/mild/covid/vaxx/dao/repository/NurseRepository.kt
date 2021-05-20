package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Nurses
import blue.mild.covid.vaxx.dto.response.PersonnelDtoOut
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

@Suppress("LongParameterList") // it's a repository, we're fine with this
class NurseRepository {
    /**
     * Returns all nurses in the database.
     */
    suspend fun getAll(): List<PersonnelDtoOut> = newSuspendedTransaction {
        Nurses.selectAll().map {
            PersonnelDtoOut(
                id = it[Nurses.id],
                firstName = it[Nurses.firstName],
                lastName = it[Nurses.lastName],
                email = it[Nurses.email]
            )
        }
    }

    /**
     * Creates new nurse and returns its ID.
     */
    suspend fun saveNurse(
        firstName: String,
        lastName: String,
        email: String
    ): EntityId = newSuspendedTransaction {
        val nurse = Nurses.insert {
            it[Nurses.firstName] = firstName
            it[Nurses.lastName] = lastName
            it[Nurses.email] = email
        }
        nurse[Nurses.id]
    }
}
