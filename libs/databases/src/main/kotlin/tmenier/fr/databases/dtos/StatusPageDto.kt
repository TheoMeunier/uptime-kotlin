package tmenier.fr.databases.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.statuspages.StatusPageLayout
import java.time.Instant
import java.util.UUID

@RegisterForReflection
data class StoreStatusPageGroupDto(
    val name: String?,
    val probeIds: List<UUID>,
)

@RegisterForReflection
data class StoreStatusPageDto(
    val id: UUID,
    val slug: String,
    val title: String,
    val description: String?,
    val defaultLayout: StatusPageLayout,
    val groups: List<StoreStatusPageGroupDto>,
)

@RegisterForReflection
data class StatusPageListItemDto(
    val id: UUID,
    val slug: String,
    val title: String,
    val description: String?,
    val defaultLayout: StatusPageLayout,
    val groupCount: Int,
    val probeCount: Int,
    val updatedAt: Instant,
)

@RegisterForReflection
data class StatusPageGroupDto(
    val name: String?,
    val probes: List<ProbeListDTO>,
)

@RegisterForReflection
data class StatusPageDto(
    val id: UUID,
    val slug: String,
    val title: String,
    val description: String?,
    val defaultLayout: StatusPageLayout,
    val groups: List<StatusPageGroupDto>,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@RegisterForReflection
data class PublicStatusPageGroupDto(
    val name: String?,
    val items: List<ProbeStatusDTO>,
)

@RegisterForReflection
data class PublicStatusPageDto(
    val slug: String,
    val title: String,
    val description: String?,
    val defaultLayout: StatusPageLayout,
    val groups: List<PublicStatusPageGroupDto>,
)
