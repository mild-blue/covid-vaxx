package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.model.Patients
import blue.mild.covid.vaxx.dto.config.MailJetConfigurationDto
import blue.mild.covid.vaxx.dto.internal.PatientEmailRequestDto
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import com.mailjet.client.resource.Emailv31
import io.ktor.http.HttpStatusCode
import mu.KLogging
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.json.JSONArray
import org.json.JSONObject
import pw.forst.katlib.TimeProvider
import pw.forst.katlib.whenNull
import java.io.StringWriter
import java.time.Instant
import freemarker.template.Configuration as FreemarkerConfiguration


class MailJetEmailService(
    private val freemarkerConfiguration: FreemarkerConfiguration,
    private val mailJetConfig: MailJetConfigurationDto,
    private val client: MailjetClient,
    private val nowProvider: TimeProvider<Instant>
) : MailService, DispatchService<PatientEmailRequestDto>(1) {

    private companion object : KLogging() {
        val SUCCESS = HttpStatusCode.OK.value
    }

    init {
        // TODO consider populating channel with unsent emails during the init
        // eager initialize during the construction
        initialize()
    }

    /**
     * Send email using MailJet service.
     */
    override suspend fun sendEmail(patientRegistrationDto: PatientEmailRequestDto) {
        insertToChannel(patientRegistrationDto)
    }

    override suspend fun dispatch(work: PatientEmailRequestDto) {
        sendMailBlocking(work)
    }

    private suspend fun sendMailBlocking(emailRequest: PatientEmailRequestDto) {
        if (emailRequest.attemptLeft == 0) {
            logger.error { "Not executing email request \"$emailRequest\" - attempts left 0!" }
        }

        logger.info { "Sending an email to ${emailRequest.email}." }

        runCatching { client.post(buildEmailRequest(emailRequest)) }
            .onFailure {
                // TODO maybe put that back to the queue
                logger.error(it) { "Sending email to ${emailRequest.email} has thrown an exception." }
            }.getOrNull()
            ?.also {
                if (it.status != SUCCESS) {
                    logger.error {
                        "Sending email to ${emailRequest.email}, patient id ${emailRequest.patientId} was not successful details: ${it.data}."
                    }
                }
            }?.takeIf { it.status == SUCCESS }
            ?.also {
                // save information about email sent to the database
                // we want to keep this transaction on this thread, so we don't suspend it
                transaction {
                    Patients.update({ Patients.id eq emailRequest.patientId }) {
                        it[registrationEmailSent] = nowProvider.now()
                    }
                }
                logger.info { "Registration mail sent for patient ${emailRequest.patientId}." }
            }.whenNull {
                dispatch(emailRequest.copy(attemptLeft = emailRequest.attemptLeft - 1))
            }
    }

    private fun buildEmailRequest(emailRequest: PatientEmailRequestDto): MailjetRequest? {
        val stringWriter = StringWriter()
        freemarkerConfiguration.getTemplate("RegistrationConfirmation.ftl")
            .apply { process(mapOf("emailRequestDto" to emailRequest), stringWriter) }

        val emailHtmlPart = stringWriter.toString()

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
                                        // TODO add slot information from emailRequest.slot
                                    )
                            )
                            .put(Emailv31.Message.SUBJECT, mailJetConfig.subject)
                            .put(
                                Emailv31.Message.HTMLPART, emailHtmlPart
                            )
                    )
            )
    }
}
