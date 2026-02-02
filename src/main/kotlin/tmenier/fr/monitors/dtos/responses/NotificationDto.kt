package tmenier.fr.monitors.dtos.responses

import java.util.UUID

data class ListingNotificationsDto(
    val id: UUID,
    val name: String,
    val isDefault: Boolean,
)

data class NotificationIdDto(
    val id: UUID,
)
