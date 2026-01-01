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
        val message =
            MailMessage()
                .setFrom(content.from)
                .setTo(content.to)
                .setSubject("✅ Probe Success: ${probe.name}")
                .setText(
                    """
                    Probe: ${probe.name}
                    Status: SUCCESS
                    Response Time: ${result.responseTime}ms
                    Timestamp: ${result.runAt}
                    
                    URL: ${probe.url}
                    """.trimIndent(),
                )

        client
            .sendMail(message)
            .onSuccess { logger.info { "Success send mail: $message" } }
            .onFailure { logger.error { "Failed to send mail: $message" } }
    }

    override fun sendFailure(
        content: NotificationContent.Mail,
        probe: ProbesEntity,
        result: ProbeResult,
    ) {
        val client = getMailClient(content)
        val message =
            MailMessage()
                .setFrom(content.from)
                .setTo(content.to)
                .setSubject("❌ Probe Failure: ${probe.name}")
                .setText(
                    """
                    Probe: ${probe.name}
                    Status: FAILURE
                    Error: ${result.message}
                    Timestamp: ${result.runAt}
                    
                    URL: ${probe.url}
                    """.trimIndent(),
                )

        client
            .sendMail(message)
            .onSuccess { logger.info { "Success send mail: $message" } }
            .onFailure { logger.error { "Failed to send mail: $message" } }
    }

    override fun sendTest(content: NotificationContent.Mail) {
        val client = getMailClient(content)
        val message =
            MailMessage()
                .setFrom(content.from)
                .setTo(content.to)
                .setSubject("Send Mail: Uptime kotlin")
                .setText(
                    """Send test mail""".trimIndent(),
                )

        client
            .sendMail(message)
            .onSuccess { logger.info { "Success send mail: $message" } }
            .onFailure { logger.error { "Failed to send mail: $message" } }
    }

    override fun getNotificationType() = NotificationChannelsEnum.MAIL.name

    private fun getMailClient(credential: NotificationContent.Mail): MailClient {
        val config =
            MailConfig().apply {
                hostname = credential.hostname
                port = credential.port
                starttls = if (credential.starttls) StartTLSOptions.REQUIRED else StartTLSOptions.DISABLED
                login = LoginOption.REQUIRED
                username = credential.username
                password = encryptionService.decrypt(credential.password)
            }

        return MailClient.createShared(vertx, config)
    }
}
