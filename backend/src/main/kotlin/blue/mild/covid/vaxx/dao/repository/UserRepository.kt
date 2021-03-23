package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.User
import blue.mild.covid.vaxx.dao.model.UserRole
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserRepository {
    /**
     * Provides view to the entity inside the transaction.
     */
    suspend fun <T> viewByUsername(
        username: String,
        viewBlock: suspend User.(ResultRow) -> T
    ): T? = newSuspendedTransaction {
        User.select { User.username eq username }
            .singleOrNull()
            ?.let { User.viewBlock(it) }
    }

    /**
     * Saves new user to the database and returns its [id].
     */
    suspend fun saveUser(
        id: EntityId,
        username: String,
        passwordHash: String,
        role: UserRole
    ): EntityId = newSuspendedTransaction {
        User.insert {
            it[User.id] = id
            it[User.username] = username
            it[User.passwordHash] = passwordHash
            it[User.role] = role
        }
        id
    }
}
