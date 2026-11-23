package tmenier.fr.twofactor.dtos.responses

import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.Instant

@RegisterForReflection
data class TwoFactorStatusResponse(
    val enabled: Boolean,
    val enabledAt: Instant?,
    val recoveryCodesRemaining: Long,
)
