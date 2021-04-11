package blue.mild.covid.vaxx.utils

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.UserRole
import pw.forst.tools.katlib.toUuid

/**
 * This object contains data that are already in the database.
 *
 * The objects are not complete but rather contain only necessary data for the tests.
 */
object DatabaseData {
    val admin = User(
        id = "c3858476-3934-4727-82f5-f9d42cea4adb".toUuid(),
        email = "vaxx@mild.blue",
        password = "bluemild",
        role = UserRole.ADMIN
    )
    val nurses = listOf(
        Nurse("3449bc47-1e02-4353-9d86-c9b4acf8889e".toUuid()),
        Nurse("5a1c1110-c485-4822-95a3-de58ef6c0dca".toUuid()),
        Nurse("f3ca381e-899c-4abf-bbee-3cc2ca7dc23c".toUuid()),
    )
}

data class User(val id: EntityId, val email: String, val password: String, val role: UserRole)
data class Nurse(val id: EntityId)
