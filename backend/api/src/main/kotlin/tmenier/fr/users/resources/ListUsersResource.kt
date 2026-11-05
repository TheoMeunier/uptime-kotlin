package tmenier.fr.users.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.users.actions.ListUsersAction
import java.util.UUID

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
class ListUsersResource(
    private val listUsersAction: ListUsersAction,
    private val jwt: JsonWebToken,
) {
    @GET
    @RolesAllowed(UserRole.ADMIN_ROLE)
    fun list(): Response = Response.ok(listUsersAction.execute(UUID.fromString(jwt.name))).build()
}
