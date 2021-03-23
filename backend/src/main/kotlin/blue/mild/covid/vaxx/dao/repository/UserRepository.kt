package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.User
import blue.mild.covid.vaxx.dao.model.UserLogins
import blue.mild.covid.vaxx.dao.model.UserRole
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserRepository {
    /**
     * Provides view to the entity inside the transaction.
     */
    suspend fun <T> viewByEmail(
        email: String,
        viewBlock: suspend User.(ResultRow) -> T
    ): T? = newSuspendedTransaction {
        User.select { User.email eq email }
            .singleOrNull()
            ?.let { User.viewBlock(it) }
    }

    /**
     * Saves new user to the database.
     */
    suspend fun saveUser(
        firstName: String,
        lastName: String,
        email: String,
        passwordHash: String,
        role: UserRole
    ): EntityId = newSuspendedTransaction {
        User.insert {
            it[User.firstName] = firstName
            it[User.lastName] = lastName
            it[User.email] = email
            it[User.passwordHash] = passwordHash
            it[User.role] = role
        }[User.id]
    }

    /**
     * Records that user logged in in [UserLogins] database.
     */
    suspend fun recordLogin(
        userId: EntityId,
        success: Boolean,
        remoteHost: String,
        callId: String?,
        vaccineSerialNumber: String? = null,
        nurseId: EntityId? = null
    ): EntityId = newSuspendedTransaction {
        UserLogins.insert {
            it[UserLogins.userId] = userId
            it[UserLogins.vaccineSerialNumber] = vaccineSerialNumber
            it[UserLogins.nurseId] = nurseId
            it[UserLogins.success] = success
            it[UserLogins.remoteHost] = remoteHost
            it[UserLogins.callId] = callId
        }[UserLogins.id]
    }
}
