package tmenier.fr.databases.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.statuspages.StatusPageLayout
import tmenier.fr.common.enums.statuspages.StatusPageUptimePeriod
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
    val uptimePeriod: StatusPageUptimePeriod,
    val isPrivate: Boolean,
    val groups: List<StoreStatusPageGroupDto>,
    val customDomain: String? = null,
)

@RegisterForReflection
data class StatusPageListItemDto(
    val id: UUID,
    val slug: String,
    val title: String,
    val description: String?,
    val defaultLayout: StatusPageLayout,
    val uptimePeriod: StatusPageUptimePeriod,
    val isPrivate: Boolean,
    val groupCount: Int,
    val probeCount: Int,
    val updatedAt: Instant,
    val customDomain: String? = null,
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
    val uptimePeriod: StatusPageUptimePeriod,
    val isPrivate: Boolean,
    val groups: List<StatusPageGroupDto>,
    val createdAt: Instant,
    val updatedAt: Instant,
    val customDomain: String? = null,
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
    val uptimePeriod: StatusPageUptimePeriod,
    val isPrivate: Boolean,
    val groups: List<PublicStatusPageGroupDto>,
)
