package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dto.MailJetConfigurationDto
import blue.mild.covid.vaxx.dto.request.PatientRegistrationDtoIn
import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import com.mailjet.client.MailjetResponse
import com.mailjet.client.resource.Emailv31
import org.json.JSONArray
import org.json.JSONObject

class EmailUserAfterRegistrationService(private val mailJetConfig: MailJetConfigurationDto){
    fun sendEmail(patient_registration_dto: PatientRegistrationDtoIn) {
        val response: MailjetResponse
        val client = MailjetClient(
            mailJetConfig.apiKey,
            mailJetConfig.apiSecret,
            ClientOptions("v3.1")
        )
        val request: MailjetRequest = MailjetRequest(Emailv31.resource)
            .property(
                Emailv31.MESSAGES, JSONArray()
                    .put(
                        JSONObject()
                            .put(
                                Emailv31.Message.FROM, JSONObject()
                                    .put("Email", mailJetConfig.emailFrom)
//                                    .put("Name", "Očkování Praha 7")
                            )
                            .put(
                                Emailv31.Message.TO, JSONArray()
                                    .put(
                                        JSONObject()
                                            .put("Email", patient_registration_dto.email)
//                                            .put("Name", "Jan")
                                    )
                            )
                            .put(Emailv31.Message.SUBJECT, "Testing Subject")
                            .put(Emailv31.Message.TEXTPART, "Dear ${patient_registration_dto.lastName}" +
                                    "You have been registered!")
                    )
            )
        response = client.post(request)
        println(response.status)
        println(response.data)
    }
}