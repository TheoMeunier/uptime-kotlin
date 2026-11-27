package tmenier.fr.schedulers.templates

import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.ProbeSchedulerInterfaceType
import java.time.Duration
import java.time.Instant

abstract class ProbeProtocolAbstract<T : ProbeContent> : ProbeSchedulerInterfaceType<T> {
    override fun check(
        probe: ProbeDTO,
        isLastAttempt: Boolean,
    ): ProbeResult {
        // contentClass.cast checks the content at runtime; the cast to T only restores the erased type.
        @Suppress("UNCHECKED_CAST")
        val content = protocol.contentClass.cast(probe.content) as T
        return execute(probe, content, isLastAttempt)
    }

    protected fun now(): Instant = Instant.now()

    protected fun getResponseTime(startDateTime: Instant) = Duration.between(startDateTime, Instant.now()).toMillis()

    protected fun timeoutSeconds(probe: ProbeDTO): Int = ProbeTimeout.seconds(probe.timeout)

    protected fun timeoutOf(probe: ProbeDTO): Duration = ProbeTimeout.duration(probe.timeout)
}
