package tmenier.fr.settings.dtos

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class DefaultStatusPageSettingsDto(
    val isPrivate: Boolean,
)
