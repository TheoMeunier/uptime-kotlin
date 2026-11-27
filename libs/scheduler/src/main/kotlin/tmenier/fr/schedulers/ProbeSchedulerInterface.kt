package tmenier.fr.schedulers

import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO

interface ProbeSchedulerInterface {
    val protocol: ProbeProtocol

    fun check(
        probe: ProbeDTO,
        isLastAttempt: Boolean,
    ): ProbeResult
}

interface ProbeSchedulerInterfaceType<T : ProbeContent> : ProbeSchedulerInterface {
    fun execute(
        probe: ProbeDTO,
        content: T,
        isLastAttempt: Boolean,
    ): ProbeResult
}
