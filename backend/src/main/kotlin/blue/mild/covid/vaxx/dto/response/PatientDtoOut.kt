package blue.mild.covid.vaxx.dto.response

import blue.mild.covid.vaxx.dao.model.EntityId
import blue.mild.covid.vaxx.dao.model.InsuranceCompany
import java.time.Instant

data class PatientDtoOut(
    val id: EntityId,
    val registeredOn: Instant,
    val firstName: String,
    val lastName: String,
    val zipCode: Int,
    val district: String,
    val personalNumber: String?,
    val insuranceNumber: String?,
    val phoneNumber: String,
    val email: String,
    val insuranceCompany: InsuranceCompany,
    val indication: String? = null,
    val answers: List<AnswerDtoOut>,
    val registrationEmailSentOn: Instant? = null,
    val vaccinated: VaccinationDtoOut? = null,
    val dataCorrect: DataCorrectnessConfirmationDtoOut? = null,
    val vaccinationSlotDtoOut: VaccinationSlotDtoOut? = null,
    val isinId: String? = null
)
