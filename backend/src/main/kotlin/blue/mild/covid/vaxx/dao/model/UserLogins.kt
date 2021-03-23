package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp

object UserLogins : Table("user_logins") {
    /**
     * Primary key.
     */
    val id = entityId("id")

    /**
     * When this record was created.
     */
    val created = timestamp("created")

    /**
     * When this record was updated. By default same value as [created].
     */
    val updated = timestamp("updated")

    /**
     * Usually an email address.
     */
    val userId = entityId("user_id") references User.id

    /**
     * Serial number of vaccine set during login.
     */
    val vaccineSerialNumber = varchar("vaccine_serial_number", DatabaseTypeLength.DEFAULT_STRING).nullable()

    /**
     * Name of the nurse.
     */
    val nurseId = (entityId("nurse_id") references Nurse.id).nullable()

    /**
     * Determines whether the login was successful or not.
     */
    val success = bool("success")

    /**
     * Remote host from which tried user log in.
     */
    val remoteHost = varchar("remote_host", DatabaseTypeLength.REMOTE_HOST)

    /**
     * Application call id that called login endpoint, can be null if call generation is disabled.
     */
    val callId = varchar("call_id", DatabaseTypeLength.DEFAULT_STRING).nullable()

    override val primaryKey = PrimaryKey(id)
}
