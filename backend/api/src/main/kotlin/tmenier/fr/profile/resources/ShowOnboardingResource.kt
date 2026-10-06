package tmenier.fr.profile.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.jwt.JsonWebToken
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
    @Authenticated
    fun show(): OnboardingResponse = OnboardingResponse(userRepository.isOnboardingCompleted(UUID.fromString(jwt.name)))
}
