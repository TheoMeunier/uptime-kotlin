package tmenier.fr.schedulers

import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.databases.dtos.ProbeDTO

interface ProbeSchedulerInterface {
    fun getProtocolType(): String
}

interface ProbeSchedulerInterfaceType<T> : ProbeSchedulerInterface {
    fun execute(
        probe: ProbeDTO,
        content: T,
        isLastAttempt: Boolean,
    ): ProbeResult
}
