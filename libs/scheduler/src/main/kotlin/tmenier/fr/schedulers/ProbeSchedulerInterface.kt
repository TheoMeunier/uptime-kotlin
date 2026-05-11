package tmenier.fr.schedulers

import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.schedulers.dto.ProbeResult

interface ProbeSchedulerInterface {
    fun getProtocolType(): String
}

interface ProbeSchedulerInterfaceType<T> : ProbeSchedulerInterface {
    fun execute(
        probe: ProbesEntity,
        content: T,
        isLastAttempt: Boolean,
    ): ProbeResult
}
