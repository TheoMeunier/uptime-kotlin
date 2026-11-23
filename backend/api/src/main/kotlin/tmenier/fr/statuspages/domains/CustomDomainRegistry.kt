package tmenier.fr.statuspages.domains

import io.quarkus.narayana.jta.QuarkusTransaction
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.enterprise.event.TransactionPhase
import org.jboss.logging.Logger
import tmenier.fr.databases.repositories.StatusPageRepository
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@ApplicationScoped
class CustomDomainRegistry(
    private val statusPageRepository: StatusPageRepository,
) {
    private val log = Logger.getLogger(CustomDomainRegistry::class.java)

    private val loader: ExecutorService =
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "custom-domain-registry").apply { isDaemon = true }
        }

    private val refreshing = AtomicBoolean(false)

    @Volatile
    private var snapshot = Snapshot(emptyMap(), Instant.EPOCH)

    fun slugFor(host: String?): String? {
        val current = snapshot
        if (current.loadedAt.plus(TTL).isBefore(Instant.now())) refreshInBackground()
        return host?.let { current.domains[it] }
    }

    fun isCustomDomain(host: String?): Boolean = slugFor(host) != null

    fun reload() {
        loader.submit(::load).get(RELOAD_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
    }

    fun onStart(
        @Observes event: StartupEvent,
    ) {
        runCatching { reload() }.onFailure { log.warn("Custom domains not loaded at startup, retrying on first request", it) }
    }

    fun onStop(
        @Observes event: ShutdownEvent,
    ) {
        loader.shutdownNow()
    }

    fun onStatusPagesChanged(
        @Observes(during = TransactionPhase.AFTER_SUCCESS) event: StatusPagesChanged,
    ) {
        runCatching { reload() }.onFailure { log.warn("Custom domains not reloaded after a change", it) }
    }

    private fun refreshInBackground() {
        if (!refreshing.compareAndSet(false, true)) return

        loader.execute {
            try {
                load()
            } catch (e: Exception) {
                log.warn("Custom domains refresh failed", e)
            } finally {
                refreshing.set(false)
            }
        }
    }

    private fun load() {
        val domains = QuarkusTransaction.requiringNew().call { statusPageRepository.findCustomDomains() }
        snapshot = Snapshot(domains, Instant.now())
    }

    private data class Snapshot(
        val domains: Map<String, String>,
        val loadedAt: Instant,
    )

    private companion object {
        val TTL: Duration = Duration.ofSeconds(30)
        val RELOAD_TIMEOUT: Duration = Duration.ofSeconds(10)
    }
}
