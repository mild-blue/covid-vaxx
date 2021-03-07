package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.PatientEmailRequestDto
import mu.KLogging

class DummyMailService : MailService {

    private companion object : KLogging()

    init {
        logger.warn { "Initializing DummyMailService, this should not be in production." }
    }

    override suspend fun sendEmail(patientRegistrationDto: PatientEmailRequestDto) {
        logger.warn { "NOT SENDING email to ${patientRegistrationDto.email}." }
    }
}
