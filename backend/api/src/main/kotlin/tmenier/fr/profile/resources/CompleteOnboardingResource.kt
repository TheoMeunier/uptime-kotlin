package tmenier.fr.profile.resources

import io.quarkus.security.Authenticated
import jakarta.transaction.Transactional
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.profile.dtos.responses.OnboardingResponse
import java.time.Instant
import java.util.UUID

@Path("/api/profile/onboarding/complete")
@Produces(MediaType.APPLICATION_JSON)
class CompleteOnboardingResource(
    private val jwt: JsonWebToken,
    private val userRepository: UserRepository,
) {
    @POST
    @Authenticated
    @Transactional
    fun complete(): OnboardingResponse {
        userRepository.markOnboardingCompleted(UUID.fromString(jwt.name), Instant.now())
        return OnboardingResponse(completed = true)
    }
}
