package tmenier.fr.backup.restore

import java.util.UUID

sealed interface RestoreTarget {
    val id: UUID?

    data class Existing(
        override val id: UUID,
    ) : RestoreTarget

    class Pending(
        val step: RestoreStep,
    ) : RestoreTarget {
        override val id: UUID? get() = step.createdId
    }
}
