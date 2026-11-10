package tmenier.fr.notifications.templates

import io.vertx.core.Vertx
import io.vertx.ext.mail.LoginOption
import io.vertx.ext.mail.MailClient
import io.vertx.ext.mail.MailConfig
import io.vertx.ext.mail.MailMessage
import io.vertx.ext.mail.StartTLSOptions
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.common.utils.toHumanReadable
import tmenier.fr.common.validations.EmailListValidator
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.notifications.NotificationDateFormatter
import tmenier.fr.notifications.templates.EmailTemplate.Row
import tmenier.fr.notifications.templates.EmailTemplate.Tone
import java.time.Duration
import java.time.Instant

@ApplicationScoped
class EmailNotificationService(
    private val vertx: Vertx,
    private val logger: Logger,
    private val encryptionService: EncryptionService,
    private val dates: NotificationDateFormatter,
    @param:ConfigProperty(name = "uptime.version", defaultValue = "dev")
    version: String,
) : tmenier.fr.notifications.TypedNotificationInterfaces<NotificationContent.Mail> {
    private val version: String? = version.trim().takeUnless { it.isEmpty() || it == "dev" }

    override fun sendSuccess(
        content: NotificationContent.Mail,
        probe: ProbeDTO,
        result: ProbeResult,
        downtime: Duration?,
    ) {
        send(content, successEmail(probe, result, downtime))
        logger.info("Success mail sent to ${content.to} for probe ${probe.name}")
    }

    override fun sendFailure(
        content: NotificationContent.Mail,
        probe: ProbeDTO,
        result: ProbeResult,
    ) {
        send(content, failureEmail(probe, result))
        logger.info("Failure notification sent to ${content.to} for probe ${probe.name}")
    }

    override fun sendReminder(
        content: NotificationContent.Mail,
        probe: ProbeDTO,
        result: ProbeResult,
        reminderIndex: Int,
    ) {
        send(content, reminderEmail(probe, result, reminderIndex))
        logger.info("Reminder #$reminderIndex sent to ${content.to} for probe ${probe.name}")
    }

    override fun sendTest(content: NotificationContent.Mail) {
        send(content, testEmail(content, Instant.now()))
        logger.info("Test mail sent successfully to ${content.to}")
    }

    override fun getNotificationType() = NotificationChannelsEnum.MAIL.name

    internal fun failureEmail(
        probe: ProbeDTO,
        result: ProbeResult,
    ): EmailBody {
        val degraded = result.status == ProbeMonitorLogStatus.WARNING
        val state = if (degraded) "degraded" else "down"
        return EmailTemplate.render(
            EmailTemplate.Content(
                tone = if (degraded) Tone.DEGRADED else Tone.DOWN,
                badge = if (degraded) "Degraded" else "Down",
                subject = "${if (degraded) "🟡" else "🔴"} ${probe.name} is $state",
                headline = "${probe.name} is $state",
                intro = "Uptime Kotlin detected a problem on this monitor at ${dates.format(result.runAt)}.",
                rows =
                    probeRows(probe) +
                        listOfNotNull(
                            result.statusCode?.let { Row("HTTP status", it.toString()) },
                            result.responseTime.takeIf { it > 0 }?.let { Row("Response time", "$it ms") },
                            Row("Detected at", dates.format(result.runAt)),
                        ),
                detailsLabel = "Error",
                details = result.message,
                version = version,
            ),
        )
    }

    internal fun reminderEmail(
        probe: ProbeDTO,
        result: ProbeResult,
        reminderIndex: Int,
    ): EmailBody =
        EmailTemplate.render(
            EmailTemplate.Content(
                tone = Tone.STILL_DOWN,
                badge = "Still down · reminder #$reminderIndex",
                subject = "🟠 ${probe.name} is still down (reminder #$reminderIndex)",
                headline = "${probe.name} is still down",
                intro = "The monitor has not recovered yet. Last check: ${dates.format(result.runAt)}.",
                rows =
                    probeRows(probe) +
                        listOfNotNull(
                            result.statusCode?.let { Row("HTTP status", it.toString()) },
                            Row("Reminder", "#$reminderIndex"),
                            Row("Last check", dates.format(result.runAt)),
                        ),
                detailsLabel = "Last error",
                details = result.message,
                version = version,
            ),
        )

    internal fun successEmail(
        probe: ProbeDTO,
        result: ProbeResult,
        downtime: Duration?,
    ): EmailBody {
        val down = downtime?.toHumanReadable()
        return EmailTemplate.render(
            EmailTemplate.Content(
                tone = Tone.UP,
                badge = "Up",
                subject = "✅ ${probe.name} is back up" + (down?.let { " (down for $it)" } ?: ""),
                headline = "${probe.name} is back up",
                intro =
                    down?.let { "The monitor recovered after $it of downtime." }
                        ?: "The monitor is responding normally again.",
                rows =
                    probeRows(probe) +
                        listOfNotNull(
                            down?.let { Row("Downtime", it) },
                            result.statusCode?.let { Row("HTTP status", it.toString()) },
                            Row("Response time", "${result.responseTime} ms"),
                            Row("Recovered at", dates.format(result.runAt)),
                        ),
                version = version,
            ),
        )
    }

    internal fun testEmail(
        content: NotificationContent.Mail,
        at: Instant,
    ): EmailBody =
        EmailTemplate.render(
            EmailTemplate.Content(
                tone = Tone.TEST,
                badge = "Test",
                subject = "Uptime Kotlin · test e-mail",
                headline = "Your e-mail notifications are working",
                intro = "This is a test message sent from Uptime Kotlin. Alerts for your monitors will look like this.",
                rows =
                    listOf(
                        Row("SMTP server", "${cleanHostname(content.hostname)}:${content.port}", monospace = true),
                        Row("From", content.from),
                        Row("Sent at", dates.format(at)),
                    ),
                version = version,
            ),
        )

    private fun probeRows(probe: ProbeDTO): List<Row> =
        listOfNotNull(
            Row("Monitor", probe.name),
            probe.url?.takeIf { it.isNotBlank() }?.let { Row("Target", it, monospace = true) },
            Row("Type", probe.protocol.value),
        )

    private fun send(
        content: NotificationContent.Mail,
        email: EmailBody,
    ) {
        val message =
            MailMessage()
                .setFrom(content.from)
                .setTo(EmailListValidator.parse(content.to))
                .setSubject(email.subject)
                .setText(email.text)
                .setHtml(email.html)
        content.cc
            ?.filter { it.isNotBlank() }
            ?.takeIf { it.isNotEmpty() }
            ?.let { message.setCc(it) }

        getMailClient(content)
            .sendMail(message)
            .toCompletionStage()
            .toCompletableFuture()
            .join()
    }

    private fun cleanHostname(hostname: String) = hostname.removePrefix("https://").removePrefix("http://")

    private fun getMailClient(credential: NotificationContent.Mail): MailClient {
        val password = encryptionService.decryptIfEncrypted(credential.password!!)

        val config =
            MailConfig().apply {
                this.hostname = cleanHostname(credential.hostname)
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
