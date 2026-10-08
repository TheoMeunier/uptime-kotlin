package tmenier.fr.notifications.requests

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.groups.Default
import org.hibernate.validator.constraints.URL
import tmenier.fr.common.enums.monitors.HttpMethodEnum
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum

// Extending Default keeps the unscoped constraints (URL, e-mail, patterns) active on create and update.
interface OnCreate : Default

interface OnUpdate : Default

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "notification_type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = ValidNotificationChannelDiscordRequest::class, name = "DISCORD"),
    JsonSubTypes.Type(value = ValidNotificationChannelTeamsRequest::class, name = "TEAMS"),
    JsonSubTypes.Type(value = ValidNotificationChannelMailRequest::class, name = "MAIL"),
    JsonSubTypes.Type(value = ValidNotificationChannelSlackRequest::class, name = "SLACK"),
    JsonSubTypes.Type(value = ValidNotificationChannelWebhookRequest::class, name = "WEBHOOK"),
    JsonSubTypes.Type(value = ValidNotificationChannelTelegramRequest::class, name = "TELEGRAM"),
    JsonSubTypes.Type(value = ValidNotificationChannelNtfyRequest::class, name = "NTFY"),
    JsonSubTypes.Type(value = ValidNotificationChannelGotifyRequest::class, name = "GOTIFY"),
)
@RegisterForReflection
abstract class BaseStoreNotificationRequest {
    @field:NotBlank(message = "Name is required")
    lateinit var name: String

    @field:NotNull(message = "Type notification channel is required")
    lateinit var notificationType: NotificationChannelsEnum

    @field:NotNull(message = "Is default is required")
    var isDefault: Boolean? = false
}

@RegisterForReflection
data class ValidNotificationChannelDiscordRequest(
    @field:URL(message = "Invalid URL format")
    val webhookUrl: String,
    var username: String? = null,
) : BaseStoreNotificationRequest()

@RegisterForReflection
data class ValidNotificationChannelTeamsRequest(
    @field:URL(message = "Invalid URL format")
    val webhookUrl: String,
    var username: String? = null,
) : BaseStoreNotificationRequest()

@RegisterForReflection
data class ValidNotificationChannelWebhookRequest(
    @field:URL(message = "Invalid URL format")
    val url: String,
    val method: HttpMethodEnum,
) : BaseStoreNotificationRequest()

@RegisterForReflection
data class ValidNotificationChannelSlackRequest(
    @field:URL(message = "Invalid URL format")
    val webhookUrl: String,
    var username: String? = null,
) : BaseStoreNotificationRequest()

@RegisterForReflection
data class ValidNotificationChannelTelegramRequest(
    @field:NotBlank(message = "Bot token is required", groups = [OnCreate::class])
    @field:Pattern(regexp = "^(\\d+:[A-Za-z0-9_-]+)?$", message = "Invalid bot token format")
    val botToken: String? = null,
    @field:NotBlank(message = "Chat id is required")
    @field:Pattern(regexp = "^(-?\\d+|@[A-Za-z][A-Za-z0-9_]{3,})$", message = "Invalid chat id")
    val chatId: String,
    @field:Min(1)
    val messageThreadId: Long? = null,
) : BaseStoreNotificationRequest()

@RegisterForReflection
data class ValidNotificationChannelNtfyRequest(
    @field:NotBlank(message = "Server URL is required")
    @field:URL(message = "Invalid URL format")
    val serverUrl: String,
    @field:NotBlank(message = "Topic is required")
    @field:Pattern(regexp = "^[-_A-Za-z0-9]{1,64}$", message = "Invalid topic")
    val topic: String,
    val accessToken: String? = null,
    val removeAccessToken: Boolean? = false,
) : BaseStoreNotificationRequest()

@RegisterForReflection
data class ValidNotificationChannelGotifyRequest(
    @field:NotBlank(message = "Server URL is required")
    @field:URL(message = "Invalid URL format")
    val serverUrl: String,
    @field:NotBlank(message = "Application token is required", groups = [OnCreate::class])
    val appToken: String? = null,
) : BaseStoreNotificationRequest()

@RegisterForReflection
data class ValidNotificationChannelMailRequest(
    @field:URL(message = "Invalid URL format")
    val hostname: String,
    @field:Min(1)
    @field:NotNull(message = "Port is required")
    var port: Int,
    var starttls: Boolean? = false,
    @field:NotBlank(message = "Username is required")
    var username: String,
    @field:NotBlank(message = "Password is required", groups = [OnCreate::class])
    var password: String? = null,
    @field:NotBlank(message = "From address is required")
    @field:Email(message = "Invalid email format")
    var from: String,
    @field:NotBlank(message = "To address is required")
    @field:Email(message = "Invalid email format")
    var to: String,
) : BaseStoreNotificationRequest()
