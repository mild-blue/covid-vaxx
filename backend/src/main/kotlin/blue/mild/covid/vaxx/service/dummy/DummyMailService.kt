package blue.mild.covid.vaxx.service.dummy

import blue.mild.covid.vaxx.dto.internal.PatientEmailRequestDto
import blue.mild.covid.vaxx.service.MailService
import mu.KLogging

class DummyMailService : MailService {

    private companion object : KLogging()

    override suspend fun sendEmail(patientRegistrationDto: PatientEmailRequestDto) {
        logger.warn { "NOT SENDING email to ${patientRegistrationDto.email}." }
    }
}
