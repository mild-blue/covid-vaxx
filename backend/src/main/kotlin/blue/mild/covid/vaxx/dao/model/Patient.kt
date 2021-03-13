package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp

object Patient : Table("patients") {
    val id = varchar("id", DatabaseTypeLength.ID)
    val created = timestamp("created")
    val updated = timestamp("updated")

    val firstName = varchar("first_name", DatabaseTypeLength.DEFAULT_STRING)
    val lastName = varchar("last_name", DatabaseTypeLength.DEFAULT_STRING)
    val personalNumber = varchar("personal_number", DatabaseTypeLength.PERSONAL_NUMBER)
    val phoneNumber = varchar("phone_number", DatabaseTypeLength.PHONE_NUMBER)
    val email = text("email", "citext")
    val insuranceCompany = enumerationByName("insurance_company", DatabaseTypeLength.INSURANCE_COMPANY, InsuranceCompany::class)
    val remoteHost = varchar("remote_host", DatabaseTypeLength.REMOTE_HOST)
    val registrationEmailSent = timestamp("email_sent_date").nullable()
    val vaccinatedOn = timestamp("vaccinated_on_date").nullable()

    override val primaryKey = PrimaryKey(id)
}
