package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.Nurse
import blue.mild.covid.vaxx.dto.response.PersonnelDtoOut
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class NurseRepository {
    /**
     * Returns all nurses in the database.
     */
    suspend fun getAll(): List<PersonnelDtoOut> = newSuspendedTransaction {
        Nurse.selectAll().map {
            PersonnelDtoOut(
                id = it[Nurse.id],
                firstName = it[Nurse.firstName],
                lastName = it[Nurse.lastName],
                email = it[Nurse.email]
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
        val nurse = Nurse.insert {
            it[Nurse.firstName] = firstName
            it[Nurse.lastName] = lastName
            it[Nurse.email] = email
        }
        nurse[Nurse.id]
    }
}
