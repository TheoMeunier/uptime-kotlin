package tmenier.fr.databases.dtos

import io.quarkus.runtime.annotations.RegisterForReflection

object LogRetention {
    const val MIN_DAYS: Int = 30
    const val MAX_DAYS: Int = 3650
    const val KEEP_FOREVER: Int = 0

    fun isValidDefault(days: Int?): Boolean = days == null || days in MIN_DAYS..MAX_DAYS

    fun isValidOverride(days: Int?): Boolean = days == null || days == KEEP_FOREVER || days in MIN_DAYS..MAX_DAYS

    fun effective(
        probeDays: Int?,
        defaultDays: Int?,
    ): Int? {
        val days = probeDays ?: defaultDays
        return days?.takeIf { it != KEEP_FOREVER }
    }
}

@RegisterForReflection
data class LogRetentionSettingsDto(
    val logRetentionDays: Int?,
)

@RegisterForReflection
data class LogRetentionPreviewDto(
    val retentionDays: Int?,
    val logsToDelete: Long,
    val totalLogs: Long,
    val oldestLogAt: java.time.Instant?,
)
