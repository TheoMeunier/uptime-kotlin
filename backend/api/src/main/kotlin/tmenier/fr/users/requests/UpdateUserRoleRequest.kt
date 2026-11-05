package tmenier.fr.users.requests

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.NotNull
import tmenier.fr.common.enums.users.UserRole

@RegisterForReflection
class UpdateUserRoleRequest {
    @field:NotNull(message = "Role is required")
    lateinit var role: UserRole
}
