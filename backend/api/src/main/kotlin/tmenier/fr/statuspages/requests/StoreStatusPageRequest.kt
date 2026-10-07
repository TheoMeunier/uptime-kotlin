package tmenier.fr.statuspages.requests

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import tmenier.fr.common.enums.statuspages.StatusPageLayout
import java.util.UUID

@RegisterForReflection
class StoreStatusPageRequest {
    @field:NotBlank(message = "Slug is required")
    @field:Size(max = 64, message = "Slug must be at most 64 characters")
    @field:Pattern(
        regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
        message = "Slug must contain lower-case letters, digits and single dashes only",
    )
    lateinit var slug: String

    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must be at most 255 characters")
    lateinit var title: String

    var description: String? = null

    @field:NotNull(message = "Default layout is required")
    var defaultLayout: StatusPageLayout = StatusPageLayout.GRID

    @field:Valid
    @field:Size(max = 50, message = "A status page holds at most 50 groups")
    var groups: List<StoreStatusPageGroupRequest> = emptyList()
}

@RegisterForReflection
class StoreStatusPageGroupRequest {
    @field:Size(max = 255, message = "Group name must be at most 255 characters")
    var name: String? = null

    var probeIds: List<UUID> = emptyList()
}
