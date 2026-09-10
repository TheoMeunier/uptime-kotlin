package tmenier.fr.common.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object MonitoringClock {
    val zone: ZoneId = ZoneId.systemDefault()

    fun toInstant(at: LocalDateTime): Instant = at.atZone(zone).toInstant()

    fun toLocal(at: Instant): LocalDateTime = LocalDateTime.ofInstant(at, zone)

    fun now(): Instant = Instant.now()
}
