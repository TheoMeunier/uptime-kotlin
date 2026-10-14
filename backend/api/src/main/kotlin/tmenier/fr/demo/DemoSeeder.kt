package tmenier.fr.demo

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.quarkus.narayana.jta.QuarkusTransaction
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.validation.Validator
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.common.config.DemoMode
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.mappers.UserDto
import tmenier.fr.databases.repositories.ProbeRepository
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.maintenances.actions.StoreMaintenanceAction
import tmenier.fr.maintenances.requests.StoreMaintenanceRequest
import tmenier.fr.monitors.actions.StoreProbeAction
import tmenier.fr.monitors.requests.BaseStoreProbeRequest
import tmenier.fr.notifications.actions.StoreNotificationAction
import tmenier.fr.notifications.requests.BaseStoreNotificationRequest
import tmenier.fr.statuspages.actions.StoreStatusPageAction
import tmenier.fr.statuspages.requests.StoreStatusPageRequest
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import tmenier.fr.monitors.requests.OnCreate as ProbeOnCreate
import tmenier.fr.notifications.requests.OnCreate as NotificationOnCreate

@ApplicationScoped
class DemoSeeder(
    private val demoMode: DemoMode,
    private val userRepository: UserRepository,
    private val probeRepository: ProbeRepository,
    private val bcryptService: BcryptService,
    private val storeNotificationAction: StoreNotificationAction,
    private val storeProbeAction: StoreProbeAction,
    private val storeStatusPageAction: StoreStatusPageAction,
    private val storeMaintenanceAction: StoreMaintenanceAction,
    private val objectMapper: ObjectMapper,
    private val validator: Validator,
) {
    fun onStart(
        @Observes event: StartupEvent,
    ) {
        if (!demoMode.enabled) return

        logger.warn { "Demo mode is ON: the API is read-only and notifications are not delivered." }

        try {
            val seeded = QuarkusTransaction.requiringNew().call { seedIfEmpty() }
            if (seeded) logger.info { "Demo data seeded, sign in with ${demoMode.email}." }
        } catch (e: Exception) {
            logger.error(e) { "Demo seed failed, nothing was written" }
        }
    }

    private fun seedIfEmpty(): Boolean {
        if (userRepository.countAll() > 0) {
            if (userRepository.findByEmailOrNull(demoMode.email) == null) {
                logger.warn { "Demo mode: users exist but none is ${demoMode.email}, the demo credentials will not work." }
            }
            return false
        }

        val seed =
            javaClass.getResourceAsStream(SEED_RESOURCE)?.use { objectMapper.readTree(it) }
                ?: error("Demo seed $SEED_RESOURCE not found")

        userRepository.store(
            UserDto(
                id = UUID.randomUUID(),
                name = seed.path("user_name").asText("Demo"),
                email = demoMode.email,
                password = bcryptService.hashPassword(demoMode.password),
            ),
        )

        storeNotificationAction.execute(
            read(seed.required("notification"), BaseStoreNotificationRequest::class.java, NotificationOnCreate::class.java),
        )

        seed.path("probes").forEach {
            storeProbeAction.execute(read(it, BaseStoreProbeRequest::class.java, ProbeOnCreate::class.java))
        }

        val probeIds = probeRepository.getAll().associate { it.name to it.id }

        seed.path("status_pages").forEach { page ->
            val node = page.deepCopy<ObjectNode>()
            node.path("groups").forEach { group ->
                (group as ObjectNode).replaceProbeNames(probeIds)
            }
            storeStatusPageAction.execute(read(node, StoreStatusPageRequest::class.java))
        }

        seed.path("maintenances").forEach { maintenance ->
            val node = maintenance.deepCopy<ObjectNode>()
            val startsInHours = node.remove("starts_in_hours")?.asLong() ?: DEFAULT_STARTS_IN_HOURS
            node.put(
                "starts_at",
                Instant
                    .now()
                    .plus(Duration.ofHours(startsInHours))
                    .truncatedTo(ChronoUnit.HOURS)
                    .toString(),
            )
            node.replaceProbeNames(probeIds)
            storeMaintenanceAction.execute(read(node, StoreMaintenanceRequest::class.java))
        }

        return true
    }

    private fun ObjectNode.replaceProbeNames(probeIds: Map<String, UUID>) {
        val names = remove("probes") ?: return
        val ids = names.map { probeIds[it.asText()] ?: error("Demo seed: unknown probe '${it.asText()}'") }
        set<JsonNode>("probe_ids", objectMapper.valueToTree(ids))
    }

    private fun <T : Any> read(
        node: JsonNode,
        type: Class<T>,
        vararg groups: Class<*>,
    ): T {
        val value = objectMapper.treeToValue(node, type)
        val violations = validator.validate(value, *groups)
        require(violations.isEmpty()) {
            "Demo seed: invalid ${value::class.simpleName}: " +
                violations.joinToString { "${it.propertyPath} ${it.message}" }
        }
        return value
    }

    companion object {
        private const val SEED_RESOURCE = "/demo/seed.json"
        private const val DEFAULT_STARTS_IN_HOURS = 24L
    }
}
