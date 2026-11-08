package tmenier.fr.users.requests

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import tmenier.fr.common.enums.users.UserRole

@RegisterForReflection
class StoreUserRequest {
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must be at most 255 characters")
    lateinit var name: String

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    @field:Size(max = 255, message = "Email must be at most 255 characters")
    lateinit var email: String

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters long")
    lateinit var password: String

    @field:NotNull(message = "Role is required")
    var role: UserRole = UserRole.VIEWER
}
