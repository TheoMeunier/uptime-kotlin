package tmenier.fr.common.config

import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.utils.logger
import java.util.Optional

@Startup
@ApplicationScoped
class SchedulerStrategy(
    @ConfigProperty(name = "scheduler.strategy") configured: Optional<String>,
    @ConfigProperty(name = "quarkus.scheduler.strategy") legacy: Optional<String>,
) {
    val value: String = resolve(configured.orElse(null), legacy.orElse(null))

    val runsBackgroundJobs: Boolean
        get() = value == DATABASE

    init {
        if (legacy.orElse(null)?.trim() == LEGACY_DB_LOCK) {
            logger.warn {
                "quarkus.scheduler.strategy=db-lock is deprecated and will be removed: " +
                    "set scheduler.strategy=database (SCHEDULER_STRATEGY=database) instead. " +
                    "Effective strategy: $value"
            }
        }
    }

    companion object {
        const val DATABASE = "database"
        const val NONE = "none"
        const val LEGACY_DB_LOCK = "db-lock"

        fun resolve(
            configured: String?,
            legacy: String?,
        ): String {
            val explicit = configured?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
            if (explicit != null) {
                require(explicit == DATABASE || explicit == NONE) {
                    "scheduler.strategy must be '$DATABASE' or '$NONE', got '$configured'"
                }
                return explicit
            }
            return if (legacy?.trim() == LEGACY_DB_LOCK) DATABASE else NONE
        }
    }
}
