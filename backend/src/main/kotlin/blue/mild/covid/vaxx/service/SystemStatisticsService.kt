package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.PatientDataCorrectnessConfirmation
import blue.mild.covid.vaxx.dao.model.Patients
import blue.mild.covid.vaxx.dao.model.VaccinationSlots
import blue.mild.covid.vaxx.dao.model.Vaccinations
import blue.mild.covid.vaxx.dto.request.query.SystemStatisticsFilterDtoIn
import blue.mild.covid.vaxx.dto.response.SystemStatisticsDtoOut
import blue.mild.covid.vaxx.utils.defaultPostgresFrom
import blue.mild.covid.vaxx.utils.defaultPostgresTo
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class SystemStatisticsService {

    /**
     * Create system statistics with filter starting on [query].
     */
    suspend fun getSystemStatistics(query: SystemStatisticsFilterDtoIn): SystemStatisticsDtoOut = newSuspendedTransaction {
        val from = query.from ?: defaultPostgresFrom
        val to = query.to ?: defaultPostgresTo

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

        val availableSlots = VaccinationSlots.select {
            VaccinationSlots.patientId.isNull() and
                    (VaccinationSlots.from greaterEq from) and
                    (VaccinationSlots.to lessEq to)
        }.count()

        val bookedSlots = VaccinationSlots.select {
            VaccinationSlots.patientId.isNotNull() and
                    (VaccinationSlots.from greaterEq from) and
                    (VaccinationSlots.to lessEq to)
        }.count()

        SystemStatisticsDtoOut(
            vaccinatedPatientsCount = vaccinatedPatientsCount,
            patientsDataVerifiedCount = patientDataVerifiedCount,
            registrationsCount = registrationsCount,
            emailsSentCount = emailsSentCount,
            availableSlots = availableSlots,
            bookedSlots = bookedSlots
        )
    }
}
