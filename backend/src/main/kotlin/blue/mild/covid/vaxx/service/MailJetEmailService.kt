package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.Patient
import blue.mild.covid.vaxx.dto.PatientEmailRequestDto
import blue.mild.covid.vaxx.dto.config.MailJetConfigurationDto
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import com.mailjet.client.resource.Emailv31
import freemarker.template.Configuration
import io.ktor.http.HttpStatusCode
import mu.KLogging
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.json.JSONArray
import org.json.JSONObject
import pw.forst.tools.katlib.TimeProvider
import java.io.StringWriter
import java.time.Instant


class MailJetEmailService(
    private val mailJetConfig: MailJetConfigurationDto,
    private val client: MailjetClient,
    private val nowProvider: TimeProvider<Instant>
) : MailService, DispatchService<PatientEmailRequestDto>(1) {
    // TODO consider populating channel with unsent emails during the init

    private companion object : KLogging() {
        val SUCCESS = HttpStatusCode.OK.value
    }

    init {
        // eager initialize during the construction
        initialize()
    }

    override suspend fun sendEmail(patientRegistrationDto: PatientEmailRequestDto) {
        insertToChannel(patientRegistrationDto)
    }

    override suspend fun dispatch(work: PatientEmailRequestDto) {
        sendMailBlocking(work)
    }

    private fun sendMailBlocking(emailRequest: PatientEmailRequestDto) {
        logger.debug { "Sending an email to ${emailRequest.email}." }

        val response = client.post(buildEmailRequest(emailRequest))

        if (response.status != SUCCESS) {
            // TODO consider putting it back to the channel for retry
            logger.error { "Sending email to ${emailRequest.email} was not successful details: ${response.data}." }
        } else {
            logger.debug { "Email to ${emailRequest.email} sent successfully." }
            // save information about email sent to the database
            // we want to keep this transaction on this thread, so we don't suspend it
            transaction {
                Patient.update({ Patient.id eq emailRequest.patientId.toString() }) {
                    it[emailSentDate] = nowProvider.now()
                }
            }
        }
    }

    private fun buildEmailRequest(emailRequest: PatientEmailRequestDto): MailjetRequest? {
        val freemarkerConfiguration = Configuration()

        freemarkerConfiguration.setClassForTemplateLoading(this::class.java, "/templates")

        val template = freemarkerConfiguration.getTemplate("RegistrationConfirmation.ftl")
        val stringWriter = StringWriter()
        template.process(mapOf("emailRequestDto" to emailRequest), stringWriter)
        val emailHtmlPart =  stringWriter.toString()

        return MailjetRequest(Emailv31.resource)
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
                                            .put("Email", emailRequest.email)
                                            .put("Name", "${emailRequest.firstName} ${emailRequest.lastName}")
                                    )
                            )
                            .put(Emailv31.Message.SUBJECT, "Testing Subject")
                            .put(
                                Emailv31.Message.HTMLPART, emailHtmlPart
                            )
                    )
            )
    }
}
