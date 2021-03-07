package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.PatientEmailRequestDto

interface MailService {
    suspend fun sendEmail(patientRegistrationDto: PatientEmailRequestDto)
}
