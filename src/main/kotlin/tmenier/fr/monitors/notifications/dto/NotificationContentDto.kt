package tmenier.fr.monitors.notifications.dto

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
sealed interface NotificationContent {
    @RegisterForReflection
    data class Discord(
        val webhookUrl: String,
        val username: String?,
    ) : NotificationContent

    @RegisterForReflection
    data class Teams(
        val webhookUrl: String,
        val username: String?,
    ) : NotificationContent

    @RegisterForReflection
    data class Slack(
        val webhookUrl: String,
        val username: String?,
    ) : NotificationContent

    @RegisterForReflection
    data class Mail(
        val hostname: String,
        val port: Int,
        val starttls: Boolean,
        val username: String,
        val password: String,
        val from: String,
        val to: String,
        val cc: List<String>? = null,
    ) : NotificationContent
}
