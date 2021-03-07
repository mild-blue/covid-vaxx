package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.MailJetConfigurationDto
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import blue.mild.covid.vaxx.utils.createLogger
import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import com.mailjet.client.MailjetResponse
import com.mailjet.client.resource.Emailv31
import org.json.JSONArray
import org.json.JSONObject

private val logger = createLogger("MailServiceLogger")

class EmailUserAfterRegistrationService(private val mailJetConfig: MailJetConfigurationDto) {
    fun sendEmail(patient_registration_dto: PatientRegistrationDtoIn) {
        val response: MailjetResponse
        val client = MailjetClient(
            mailJetConfig.apiKey,
            mailJetConfig.apiSecret,
            ClientOptions("v3.1")
        )
        val patient_name = "${patient_registration_dto.firstName} ${patient_registration_dto.lastName}"
        val request: MailjetRequest = MailjetRequest(Emailv31.resource)
            .property(
                Emailv31.MESSAGES, JSONArray()
                    .put(
                        JSONObject()
                            .put(
                                Emailv31.Message.FROM, JSONObject()
                                    .put("Email", mailJetConfig.emailFrom)
                                    .put("Name", mailJetConfig.nameFrom)
                            )
                            .put(
                                Emailv31.Message.TO, JSONArray()
                                    .put(
                                        JSONObject()
                                            .put("Email", patient_registration_dto.email)
                                            .put("Name", patient_name)
                                    )
                            )
                            .put(Emailv31.Message.SUBJECT, "Testing Subject")
                            .put(
                                Emailv31.Message.TEXTPART, "Dear ${patient_registration_dto.lastName}" +
                                        "You have been registered!"
                            )
                    )
            )
        response = client.post(request)
        if (response.status != 200) {
            logger.error(
                "Sending email to ${patient_registration_dto.email} was not successfull details:" +
                        "${response.data}"
            )
        } else {
            logger.debug("Email to ${patient_registration_dto.email} send successfully")
        }

    }
}
