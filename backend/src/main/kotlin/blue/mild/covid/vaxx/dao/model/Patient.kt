package blue.mild.covid.vaxx.dao.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp

// lengths in the database
@Suppress("MagicNumber")
object Patient : Table("patients") {
    val id = varchar("id", 36)
    val created = timestamp("created")
    val updated = timestamp("updated")

    val firstName = varchar("first_name", 256)
    val lastName = varchar("last_name", 256)
    val personalNumber = varchar("personal_number", 11)
    val phoneNumber = varchar("phone_number", 14)
    val email = text("email", "citext")
    val insuranceCompany = enumerationByName("insurance_company", 4, InsuranceCompany::class)
    val remoteHost = varchar("remote_host", 45) // for size see https://stackoverflow.com/a/166157/7169288
    val registrationEmailSent = timestamp("email_sent_date").nullable()
    val vaccinatedOn = timestamp("vaccinated_on_date").nullable()

    override val primaryKey = PrimaryKey(id)
}
