package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.Patient
import blue.mild.covid.vaxx.dao.model.PatientDataCorrectnessConfirmation
import blue.mild.covid.vaxx.dao.model.Vaccination
import blue.mild.covid.vaxx.dto.request.query.SystemStatisticsFilterDtoIn
import blue.mild.covid.vaxx.dto.response.SystemStatisticsDtoOut
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

class SystemStatisticsService {

    private companion object {
        val defaultFrom: Instant = Instant.EPOCH

        // this is maximal value for the Postgres
        val defaultTo: Instant = Instant.ofEpochSecond(365241780471L)
    }

    /**
     * Create system statistics with filter starting on [query].
     */
    suspend fun getSystemStatistics(query: SystemStatisticsFilterDtoIn): SystemStatisticsDtoOut = newSuspendedTransaction {
        val from = query.from ?: defaultFrom
        val to = query.to ?: defaultTo

        val vaccinatedPatients = Vaccination.select {
            Vaccination.created.between(from, to)
        }.count()

        val patientDataVerified = PatientDataCorrectnessConfirmation.select {
            PatientDataCorrectnessConfirmation.created.between(from, to)
        }.count()

        val registrationsCount = Patient.select {
            Patient.created.between(from, to)
        }.count()

        val emailSent = Patient.select {
            Patient.registrationEmailSent.isNotNull() and (Patient.registrationEmailSent.between(from, to))
        }.count()

        SystemStatisticsDtoOut(
            vaccinatedPatientsCount = vaccinatedPatients,
            patientsDataVerifiedCount = patientDataVerified,
            registrationsCount = registrationsCount,
            emailsSent = emailSent
        )
    }
}
