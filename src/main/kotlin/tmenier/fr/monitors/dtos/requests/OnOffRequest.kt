package tmenier.fr.monitors.dtos.requests

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.NotNull

@RegisterForReflection
data class OnOffRequest(
    @field:NotNull(message = "enabled must not be null")
    var enabled: Boolean,
)
