package tmenier.fr.settings.dtos.requests

import com.fasterxml.jackson.annotation.JsonIgnore
import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.AssertTrue
import tmenier.fr.databases.dtos.LogRetention

@RegisterForReflection
data class UpdateLogRetentionRequest(
    val logRetentionDays: Int? = null,
) {
    @AssertTrue(message = "Log retention must be empty (keep forever) or between 30 and 3650 days")
    @JsonIgnore
    fun isLogRetentionValid(): Boolean = LogRetention.isValidDefault(logRetentionDays)
}
