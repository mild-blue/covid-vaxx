package blue.mild.covid.vaxx.dto.response

data class SystemStatisticsDtoOut(
    val vaccinatedPatientsCount: Long,
    val patientsDataVerifiedCount: Long,
    val registrationsCount: Long,
    val emailsSent: Long
)
