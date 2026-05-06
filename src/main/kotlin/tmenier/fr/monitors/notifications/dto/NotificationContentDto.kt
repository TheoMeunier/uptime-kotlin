package tmenier.fr.monitors.notifications.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.monitors.enums.HttpMethodEnum

@RegisterForReflection
sealed interface NotificationContent {
    @RegisterForReflection
    data class Discord(
        val webhookUrl: String,
        val username: String?,
    ) : NotificationContent

    @RegisterForReflection
    data class Webhook(
        val method: HttpMethodEnum,
        val url: String,
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
        @field:JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        val password: String? = null,
        val from: String,
        val to: String,
        val cc: List<String>? = null,
    ) : NotificationContent
}
