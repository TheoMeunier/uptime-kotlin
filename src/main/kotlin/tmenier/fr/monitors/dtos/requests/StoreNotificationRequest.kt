package tmenier.fr.monitors.dtos.requests

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.URL
import tmenier.fr.monitors.enums.NotificationChannelsEnum

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "notification_type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = ValidNotificationChannelDiscordRequest::class, name = "DISCORD"),
    JsonSubTypes.Type(value = ValidNotificationChannelTeamsRequest::class, name = "TEAMS"),
    JsonSubTypes.Type(value = ValidNotificationChannelMailRequest::class, name = "MAIL"),
    JsonSubTypes.Type(value = ValidNotificationChannelSlackRequest::class, name = "SLACK"),
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
    val urlWebhook: String,
    var nameReboot: String? = null,
) : BaseStoreNotificationRequest()

@RegisterForReflection
data class ValidNotificationChannelTeamsRequest(
    @field:URL(message = "Invalid URL format")
    val urlWebhook: String,
    var nameReboot: String? = null,
) : BaseStoreNotificationRequest()

@RegisterForReflection
data class ValidNotificationChannelSlackRequest(
    @field:URL(message = "Invalid URL format")
    val urlWebhook: String,
    var nameReboot: String? = null,
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
    @field:NotBlank(message = "Password is required")
    var password: String,
    @field:NotBlank(message = "From address is required")
    @field:Email(message = "Invalid email format")
    var mailFrom: String,
    @field:NotBlank(message = "To address is required")
    @field:Email(message = "Invalid email format")
    var mailTo: String,
) : BaseStoreNotificationRequest()
