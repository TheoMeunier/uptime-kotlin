package tmenier.fr.backup.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.backup.actions.CreateBackupAction
import tmenier.fr.common.enums.backup.SecretsMode
import java.time.LocalDate
import java.time.ZoneOffset

@Path("/api/backup")
class DownloadBackupResource(
    private val createBackupAction: CreateBackupAction,
) {
    @GET
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    fun download(
        @QueryParam("secrets") @DefaultValue("encrypted") secrets: String,
    ): Response {
        val document = createBackupAction.execute(SecretsMode.fromQuery(secrets))
        val fileName = "uptime-kotlin-backup-${LocalDate.now(ZoneOffset.UTC)}.json"

        return Response
            .ok(document)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .build()
    }
}
