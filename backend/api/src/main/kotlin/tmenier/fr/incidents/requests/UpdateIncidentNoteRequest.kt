package tmenier.fr.incidents.requests

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.Size

@RegisterForReflection
class UpdateIncidentNoteRequest {
    @field:Size(max = NOTE_MAX_LENGTH, message = "The note must be at most 2000 characters")
    var note: String? = null

    companion object {
        const val NOTE_MAX_LENGTH = 2000
    }
}
