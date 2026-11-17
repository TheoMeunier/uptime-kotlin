package tmenier.fr.profile.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.profile.actions.UpdateProfileAction
import tmenier.fr.profile.dtos.requests.UpdateProfileRequest

@Path("/api/profile/update")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UpdateProfileResources(
    private val updateProfile: UpdateProfileAction,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE, UserRole.VIEWER_ROLE)
    @Transactional
    fun update(
        @Valid payload: UpdateProfileRequest,
    ): Response {
        updateProfile.execute(payload)
        return Response.ok().build()
    }
}
