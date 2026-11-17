package tmenier.fr.profile.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.profile.dtos.responses.OnboardingResponse
import java.util.UUID

@Path("/api/profile/onboarding")
@Produces(MediaType.APPLICATION_JSON)
class ShowOnboardingResource(
    private val jwt: JsonWebToken,
    private val userRepository: UserRepository,
) {
    @GET
    @RolesAllowed(UserRole.ADMIN_ROLE, UserRole.VIEWER_ROLE)
    fun show(): OnboardingResponse = OnboardingResponse(userRepository.isOnboardingCompleted(UUID.fromString(jwt.name)))
}
