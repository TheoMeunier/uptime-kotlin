package tmenier.fr.monitors.notifications.dto

sealed interface NotificationContent {
    data class Discord(
        val webhookUrl: String,
        val username: String?,
    ) : NotificationContent

    data class Teams(
        val webhookUrl: String,
        val username: String?,
    ) : NotificationContent

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
