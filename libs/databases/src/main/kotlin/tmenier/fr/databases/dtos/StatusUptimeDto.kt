package tmenier.fr.databases.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.statuspages.StatusPageUptimePeriod
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

data class UptimeBucketRow(
    val probeId: UUID,
    val localStart: LocalDateTime,
    val total: Long,
    val successes: Long,
    val failures: Long,
    val warnings: Long,
    val maintenance: Long,
)

@RegisterForReflection
data class UptimeBucketDto(
    val startsAt: Instant,
    val endsAt: Instant,
    val total: Long,
    val successes: Long,
    val failures: Long,
    val warnings: Long,
    val maintenance: Long,
)

@RegisterForReflection
data class ProbeUptimeBarDto(
    val probeId: UUID,
    val uptimePercent: Double?,
    val buckets: List<UptimeBucketDto>,
)

@RegisterForReflection
data class PublicStatusUptimeDto(
    val period: StatusPageUptimePeriod,
    val timezone: String,
    val probes: List<ProbeUptimeBarDto>,
)
