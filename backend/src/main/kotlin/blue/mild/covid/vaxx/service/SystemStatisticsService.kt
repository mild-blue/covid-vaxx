package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.PatientDataCorrectnessConfirmation
import blue.mild.covid.vaxx.dao.model.Patients
import blue.mild.covid.vaxx.dao.model.Vaccinations
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

        val vaccinatedPatientsCount = Vaccinations.select {
            Vaccinations.created.between(from, to)
        }.count()

        val patientDataVerifiedCount = PatientDataCorrectnessConfirmation.select {
            PatientDataCorrectnessConfirmation.created.between(from, to)
        }.count()

        val registrationsCount = Patients.select {
            Patients.created.between(from, to)
        }.count()

        val emailsSentCount = Patients.select {
            Patients.registrationEmailSent.isNotNull() and (Patients.registrationEmailSent.between(from, to))
        }.count()

        SystemStatisticsDtoOut(
            vaccinatedPatientsCount = vaccinatedPatientsCount,
            patientsDataVerifiedCount = patientDataVerifiedCount,
            registrationsCount = registrationsCount,
            emailsSentCount = emailsSentCount
        )
    }
}
