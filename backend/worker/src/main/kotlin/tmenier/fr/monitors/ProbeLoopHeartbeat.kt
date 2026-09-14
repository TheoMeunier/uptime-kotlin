package tmenier.fr.monitors

import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

@ApplicationScoped
class ProbeLoopHeartbeat {
    private val startedAt: Instant = Instant.now()
    private val lastTickAt = AtomicReference<Instant?>(null)

    fun tick() {
        lastTickAt.set(Instant.now())
    }

    fun lastTickAt(): Instant? = lastTickAt.get()

    fun lastActivityAt(): Instant = lastTickAt.get() ?: startedAt

    fun hasTicked(): Boolean = lastTickAt.get() != null
}
