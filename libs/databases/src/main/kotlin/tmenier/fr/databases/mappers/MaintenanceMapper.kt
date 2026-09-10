package tmenier.fr.databases.mappers

import tmenier.fr.databases.dtos.MaintenanceOccurrenceDto
import tmenier.fr.databases.dtos.MaintenanceWindowDto
import tmenier.fr.databases.dtos.MaintenanceWindowListDto
import tmenier.fr.databases.dtos.StoreMaintenanceWindowDto
import tmenier.fr.databases.entities.MaintenanceOccurrenceEntity
import tmenier.fr.databases.entities.MaintenanceWindowEntity

object MaintenanceMapper {
    fun toEntity(dto: StoreMaintenanceWindowDto): MaintenanceWindowEntity =
        MaintenanceWindowEntity().apply {
            id = dto.id
            title = dto.title
            description = dto.description
            startsAt = dto.startsAt
            durationSeconds = dto.durationSeconds
            recurrence = dto.recurrence
            recurrenceUntil = dto.recurrenceUntil
            timezone = dto.timezone
            active = dto.active
            isPublic = dto.isPublic
        }

    fun toOccurrenceDto(entity: MaintenanceOccurrenceEntity): MaintenanceOccurrenceDto =
        MaintenanceOccurrenceDto(
            id = entity.id,
            startsAt = entity.startsAt,
            endsAt = entity.endsAt,
            cancelled = entity.cancelled,
        )

    fun toDto(
        entity: MaintenanceWindowEntity,
        occurrences: List<MaintenanceOccurrenceEntity> = emptyList(),
        current: MaintenanceOccurrenceEntity? = null,
        next: MaintenanceOccurrenceEntity? = null,
    ): MaintenanceWindowDto =
        MaintenanceWindowDto(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            startsAt = entity.startsAt,
            durationSeconds = entity.durationSeconds,
            recurrence = entity.recurrence,
            recurrenceUntil = entity.recurrenceUntil,
            timezone = entity.timezone,
            active = entity.active,
            isPublic = entity.isPublic,
            probes = entity.probes.map(ProbeMapper::toProbeListDto).sortedBy { it.name.lowercase() },
            probeIds = entity.probes.map { it.id },
            currentOccurrence = current?.let(::toOccurrenceDto),
            nextOccurrence = next?.let(::toOccurrenceDto),
            upcomingOccurrences = occurrences.map(::toOccurrenceDto),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toListDto(
        entity: MaintenanceWindowEntity,
        current: MaintenanceOccurrenceEntity? = null,
        next: MaintenanceOccurrenceEntity? = null,
    ): MaintenanceWindowListDto =
        MaintenanceWindowListDto(
            id = entity.id,
            title = entity.title,
            recurrence = entity.recurrence,
            durationSeconds = entity.durationSeconds,
            timezone = entity.timezone,
            active = entity.active,
            isPublic = entity.isPublic,
            probeCount = entity.probes.size,
            currentOccurrence = current?.let(::toOccurrenceDto),
            nextOccurrence = next?.let(::toOccurrenceDto),
        )
}
