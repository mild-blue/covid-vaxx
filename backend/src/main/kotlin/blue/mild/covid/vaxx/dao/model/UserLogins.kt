package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.`java-time`.date

/**
 * Table that contains login requests.
 */
object UserLogins : ManagedTable("user_logins") {
    /**
     * Usually an email address.
     */
    val userId = userReference()

    /**
     * Serial number of vaccine set during login.
     */
    val vaccineSerialNumber = varchar("vaccine_serial_number", DatabaseTypeLength.DEFAULT_STRING).nullable()

    /**
     * When the vaccine expires.
     */
    val vaccineExpiration = date("vaccine_expiration").nullable()

    /**
     * Name of the nurse.
     */
    val nurseId = nurseReference().nullable()

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
}
