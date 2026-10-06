package tmenier.fr.schedulers.templates

import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.ProbeSchedulerInterfaceType
import java.time.Duration
import java.time.Instant

abstract class ProbeProtocolAbstract<T> : ProbeSchedulerInterfaceType<T> {
    protected fun now(): Instant = Instant.now()

    protected fun getResponseTime(startDateTime: Instant) = Duration.between(startDateTime, Instant.now()).toMillis()

    protected fun timeoutSeconds(probe: ProbeDTO): Int = ProbeTimeout.seconds(probe.timeout)

    protected fun timeoutOf(probe: ProbeDTO): Duration = ProbeTimeout.duration(probe.timeout)
}
