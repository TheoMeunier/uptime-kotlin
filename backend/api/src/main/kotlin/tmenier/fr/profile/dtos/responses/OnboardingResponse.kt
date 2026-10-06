package tmenier.fr.profile.dtos.responses

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class OnboardingResponse(
    val completed: Boolean,
)
