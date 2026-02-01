package tmenier.fr.monitors.notifications.templates

import io.vertx.core.Vertx
import io.vertx.ext.mail.*
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.logging.Logger
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.enums.NotificationChannelsEnum
import tmenier.fr.monitors.notifications.TypedNotificationInterfaces
import tmenier.fr.monitors.notifications.dto.NotificationContent
import tmenier.fr.monitors.schedulers.dto.ProbeResult

@ApplicationScoped
class EmailNotificationService(
    private val vertx: Vertx,
    private val logger: Logger,
    private val encryptionService: EncryptionService,
) : TypedNotificationInterfaces<NotificationContent.Mail> {
    override fun sendSuccess(
        content: NotificationContent.Mail,
        probe: ProbesEntity,
        result: ProbeResult,
    ) {
        val client = getMailClient(content)

        try {
            val message =
                MailMessage()
                    .setFrom(content.from)
                    .setTo(content.to)
                    .setSubject("✅ Monitor Success: ${probe.name}")
                    .setText(
                        """
                        Monitor: ${probe.name}
                        Status: SUCCESS
                        Response Time: ${result.responseTime}ms
                        Timestamp: ${result.runAt}
                        """.trimIndent(),
                    )

            client
                .sendMail(message)
                .onSuccess {
                    logger.info("Success send mail to ${content.to} for probe ${probe.name}")
                }.onFailure { error ->
                    logger.error("Failed to send success mail to ${content.to}: ${error.message}", error)
                }
        } catch (e: Exception) {
            logger.error("Exception while sending success mail: ${e.message}", e)
        }
    }

    override fun sendFailure(
        content: NotificationContent.Mail,
        probe: ProbesEntity,
        result: ProbeResult,
    ) {
        val client = getMailClient(content)

        try {
            val message =
                MailMessage()
                    .setFrom(content.from)
                    .setTo(content.to)
                    .setSubject("❌ Monitor Failure: ${probe.name}")
                    .setText(
                        """
                        Monitor: ${probe.name}
                        Status: FAILURE
                        Error: ${result.message}
                        Timestamp: ${result.runAt}
                        """.trimIndent(),
                    )

            client
                .sendMail(message)
                .onSuccess {
                    logger.info("Failure notification sent to ${content.to} for probe ${probe.name}")
                }.onFailure { error ->
                    logger.error("Failed to send failure mail to ${content.to}: ${error.message}", error)
                }
        } catch (e: Exception) {
            logger.error("Exception while sending failure mail: ${e.message}", e)
        }
    }

    override fun sendTest(content: NotificationContent.Mail) {
        val client = getMailClient(content)
        val message =
            MailMessage()
                .setFrom(content.from)
                .setTo(content.to)
                .setSubject("Send Mail: Uptime kotlin")
                .setText("Send test mail")

        client
            .sendMail(message)
            .onSuccess {
                logger.info("Test mail sent successfully to ${content.to}")
            }.onFailure { error ->
                logger.error("Failed to send test mail to ${content.to}: ${error.message}", error)
            }
    }

    override fun getNotificationType() = NotificationChannelsEnum.MAIL.name

    private fun getMailClient(credential: NotificationContent.Mail): MailClient {
        val password = encryptionService.decrypt(credential.password)
        val hostname = credential.hostname.removePrefix("https://").removePrefix("http://")

        val config =
            MailConfig().apply {
                this.hostname = hostname
                port = credential.port
                starttls = if (credential.starttls) StartTLSOptions.REQUIRED else StartTLSOptions.DISABLED
                login = LoginOption.REQUIRED
                username = credential.username
                this.password = password
                authMethods = "LOGIN PLAIN"
            }

        return MailClient.createShared(vertx, config)
    }
}
