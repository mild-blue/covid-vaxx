package blue.mild.covid.vaxx.dao.repository

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.UserLogins
import blue.mild.covid.vaxx.dao.model.UserRole
import blue.mild.covid.vaxx.dao.model.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDate

@Suppress("LongParameterList") // it's a repository, we're fine with this
class UserRepository {
    /**
     * Provides view to the entity inside the transaction.
     */
    suspend fun <T> viewByEmail(
        email: String,
        viewBlock: suspend Users.(ResultRow) -> T
    ): T? = newSuspendedTransaction {
        Users.select { Users.email eq email }
            .singleOrNull()
            ?.let { Users.viewBlock(it) }
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
        Users.insert {
            it[Users.firstName] = firstName
            it[Users.lastName] = lastName
            it[Users.email] = email
            it[Users.passwordHash] = passwordHash
            it[Users.role] = role
        }[Users.id]
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
        vaccineExpiration: LocalDate? = null,
        nurseId: EntityId? = null
    ): EntityId = newSuspendedTransaction {
        UserLogins.insert {
            it[UserLogins.userId] = userId
            it[UserLogins.vaccineSerialNumber] = vaccineSerialNumber
            it[UserLogins.vaccineExpiration] = vaccineExpiration
            it[UserLogins.nurseId] = nurseId
            it[UserLogins.success] = success
            it[UserLogins.remoteHost] = remoteHost
            it[UserLogins.callId] = callId
        }[UserLogins.id]
    }
}
