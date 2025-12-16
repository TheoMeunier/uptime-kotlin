package tmenier.fr.monitors.schedulers

import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.schedulers.dto.ProbeResult

interface ProbeSchedulerInterface {
    fun execute(probe: ProbesEntity, isFailed: Boolean? = false): ProbeResult
    fun getProtocolType(): String
}