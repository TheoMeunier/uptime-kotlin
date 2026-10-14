package tmenier.fr.setup.resources

import com.fasterxml.jackson.annotation.JsonInclude
import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.config.DemoMode
import tmenier.fr.setup.actions.GetStatusApplicationAction

@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
data class StatusResponse(
    val status: String,
    val demo: DemoCredentials? = null,
)

@RegisterForReflection
data class DemoCredentials(
    val email: String,
    val password: String,
)

@Path("/api/app/status")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class StatusApplicationResources(
    private val getStatusApplicationAction: GetStatusApplicationAction,
    private val demoMode: DemoMode,
) {
    @GET
    fun getStatusApplication(): Response {
        val result = getStatusApplicationAction.execute()
        val status =
            StatusResponse(
                status = result.toString(),
                demo = if (demoMode.enabled) DemoCredentials(demoMode.email, demoMode.password) else null,
            )

        return Response.ok(status).build()
    }
}
