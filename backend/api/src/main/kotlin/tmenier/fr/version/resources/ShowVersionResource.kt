package tmenier.fr.version.resources

import io.quarkus.runtime.annotations.RegisterForReflection
import io.quarkus.security.Authenticated
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.config.inject.ConfigProperty

@RegisterForReflection
data class VersionResponse(
    val version: String,
)

@Path("/api/version")
@Produces(MediaType.APPLICATION_JSON)
class ShowVersionResource(
    @ConfigProperty(name = "uptime.version", defaultValue = "dev")
    private val version: String,
) {
    @GET
    @Authenticated
    fun show(): VersionResponse = VersionResponse(normalize(version))

    companion object {
        fun normalize(raw: String): String {
            val trimmed = raw.trim()
            return when {
                trimmed.isEmpty() -> "dev"
                trimmed.length > 1 && (trimmed[0] == 'v' || trimmed[0] == 'V') && trimmed[1].isDigit() -> trimmed.substring(1)
                else -> trimmed
            }
        }
    }
}
