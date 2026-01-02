package tmenier.fr.monitors.schedulers.templates

import tmenier.fr.monitors.schedulers.ProbeSchedulerInterfaceType
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

abstract class ProbeProtocolAbstract<T> : ProbeSchedulerInterfaceType<T> {
    protected fun now(): Instant = Instant.now()

    protected fun getResponseTime(startDateTime: Instant) = Duration.between(startDateTime, Instant.now()).toMillis()

    protected fun getRunAt(startDateTime: Instant) = LocalDateTime.ofInstant(startDateTime, ZoneId.systemDefault())
}
