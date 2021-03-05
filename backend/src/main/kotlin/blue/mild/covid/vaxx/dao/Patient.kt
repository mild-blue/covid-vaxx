package blue.mild.covid.vaxx.dao

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp
import java.time.Instant

object Patient : Table("patients") {
    val id = varchar("id", 36)
    val created: Column<Instant> = timestamp("created")
    val updated: Column<Instant> = timestamp("updated")

    val firstName = varchar("first_name", 256)
    val lastName = varchar("last_name", 256)
    val personalNumber = varchar("personal_number", 11)
    val phoneNumber = varchar("phone_number", 13)
    val email = varchar("email", 256)
    val insuranceCompany = enumerationByName("insurance_company", 4, InsuranceCompany::class)
    val remoteHost = varchar("remote_host", 45) // for size see https://stackoverflow.com/a/166157/7169288

    override val primaryKey = PrimaryKey(id)
}
