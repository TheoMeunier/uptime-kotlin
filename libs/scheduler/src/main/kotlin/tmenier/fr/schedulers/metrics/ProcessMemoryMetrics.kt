package tmenier.fr.schedulers.metrics

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Supplier

@ApplicationScoped
class ProcessMemoryMetrics(
    private val registry: MeterRegistry,
) {
    fun register(
        @Observes event: StartupEvent,
    ) {
        if (Files.isReadable(PROC_STATUS)) {
            Gauge
                .builder(RESIDENT_MEMORY, Supplier<Number> { read(PROC_STATUS)?.let { status -> parseVmRss(status) } ?: Double.NaN })
                .description("Resident set size of the process (VmRSS)")
                .register(registry)
        }

        val limit = CGROUP_LIMITS.firstNotNullOfOrNull { path -> read(path)?.let { content -> parseCgroupLimit(content) } }
        if (limit != null) {
            Gauge
                .builder(MEMORY_LIMIT, Supplier<Number> { limit })
                .description("Memory limit of the container (cgroup), e.g. mem_limit in compose")
                .register(registry)
        }
    }

    private fun read(path: Path): String? =
        try {
            if (Files.isReadable(path)) Files.readString(path) else null
        } catch (_: Exception) {
            null
        }

    companion object {
        const val RESIDENT_MEMORY = "process.resident.memory.bytes"
        const val MEMORY_LIMIT = "process.memory.limit.bytes"

        private val PROC_STATUS: Path = Path.of("/proc/self/status")
        private val CGROUP_LIMITS: List<Path> =
            listOf(
                Path.of("/sys/fs/cgroup/memory.max"),
                Path.of("/sys/fs/cgroup/memory/memory.limit_in_bytes"),
            )

        private const val UNLIMITED_THRESHOLD: Long = 1L shl 60

        fun parseVmRss(status: String): Double? =
            status
                .lineSequence()
                .firstOrNull { it.startsWith("VmRSS:") }
                ?.removePrefix("VmRSS:")
                ?.trim()
                ?.substringBefore(' ')
                ?.toLongOrNull()
                ?.let { kiloBytes -> kiloBytes * 1024.0 }

        fun parseCgroupLimit(content: String): Double? =
            content
                .trim()
                .toLongOrNull()
                ?.takeIf { it in 1 until UNLIMITED_THRESHOLD }
                ?.toDouble()
    }
}
