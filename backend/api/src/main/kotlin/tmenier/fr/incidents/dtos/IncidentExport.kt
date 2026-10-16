package tmenier.fr.incidents.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.databases.dtos.IncidentDto
import java.time.Instant
import java.time.ZoneId

@RegisterForReflection
data class IncidentExport(
    val from: Instant,
    val to: Instant,
    val zone: ZoneId,
    val generatedAt: Instant,
    val rows: List<IncidentExportRow>,
)

@RegisterForReflection
data class IncidentExportRow(
    val incident: IncidentDto,
    val downtimeInPeriodSeconds: Long,
)
