package tmenier.fr.backup.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import tmenier.fr.backup.actions.RestoreBackupAction
import tmenier.fr.backup.dtos.BackupDocument
import tmenier.fr.backup.dtos.RestoreReport
import tmenier.fr.common.enums.backup.ConflictStrategy
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.common.exceptions.common.BadRequestException

@Path("/api/backup/restore")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class RestoreBackupResource(
    private val restoreBackupAction: RestoreBackupAction,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE)
    fun restore(
        @QueryParam("dry_run") @DefaultValue("true") dryRun: Boolean,
        @QueryParam("on_conflict") @DefaultValue("skip") onConflict: String,
        document: BackupDocument?,
    ): RestoreReport =
        restoreBackupAction.execute(
            document = document ?: throw BadRequestException("The backup file is empty"),
            strategy = ConflictStrategy.fromQuery(onConflict),
            dryRun = dryRun,
        )
}
