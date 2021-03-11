package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.PatientEmailRequestDto

interface MailService {
    /**
     * Send email about the created registration.
     */
    suspend fun sendEmail(patientRegistrationDto: PatientEmailRequestDto)
}
