package tmenier.fr.setup.dto

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class CreateFirstUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val passwordConfirmation: String,
)