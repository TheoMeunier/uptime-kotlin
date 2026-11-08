package tmenier.fr.settings.dtos.requests

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
class UpdateDefaultStatusPageRequest {
    var isPrivate: Boolean = false
}
