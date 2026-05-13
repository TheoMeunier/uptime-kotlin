package tmenier.fr.schedulers

import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.monitors.entities.ProbesEntity

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
