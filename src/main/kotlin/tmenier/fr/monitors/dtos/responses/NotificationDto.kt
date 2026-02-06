package tmenier.fr.monitors.dtos.responses

import io.quarkus.runtime.annotations.RegisterForReflection
import java.util.UUID

@RegisterForReflection
data class ListingNotificationsDto(
    val id: UUID,
    val name: String,
    val isDefault: Boolean,
)
