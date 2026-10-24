package tmenier.fr.deployments

import io.micrometer.core.instrument.MeterRegistry
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.schedulers.metrics.ProbeCheckMetrics
import tmenier.fr.schedulers.metrics.QueueMetrics
import tmenier.fr.schedulers.metrics.WorkerMetrics
import java.time.Instant
import java.util.UUID

@QuarkusTest
@TestProfile(SingleInstanceProfile::class)
class PrometheusMetricsTest {
    @Inject
    lateinit var fixtures: QueueFixtures

    @Inject
    lateinit var registry: MeterRegistry

    @Inject
    lateinit var queueMetrics: QueueMetrics

    @Inject
    lateinit var workerMetrics: WorkerMetrics

    @ConfigProperty(name = "quarkus.http.test-port", defaultValue = "8081")
    lateinit var httpPort: String

    @ConfigProperty(name = "quarkus.management.test-port", defaultValue = "9001")
    lateinit var managementPort: String

    private val probes = mutableListOf<UUID>()

    @AfterEach
    fun disableProbes() = probes.forEach(fixtures::disable)

    @Test
    fun `a Probe Check is counted and timed by protocol and result`() {
        fixtures.createTcpProbe(SingleInstanceProfile.REGION, httpPort.toInt(), intervalSeconds = 2).also(probes::add)

        val deadline = Instant.now().plusSeconds(30)
        while (successfulTcpChecks() < 1.0 && Instant.now().isBefore(deadline)) {
            Thread.sleep(250)
        }

        assertTrue(successfulTcpChecks() >= 1.0, "no successful TCP check counted within 30 s")
        val timer = registry.find(ProbeCheckMetrics.CHECK_DURATION).tag("protocol", "tcp").timer()
        assertNotNull(timer)
        assertTrue(timer!!.count() >= 1)
        val concurrency = registry.find(ProbeCheckMetrics.WORKER_CONCURRENCY).tag("region", SingleInstanceProfile.REGION).gauge()
        assertEquals(2.0, concurrency?.value())
    }

    @Test
    fun `the gauges are published on the management interface`() {
        queueMetrics.refresh()
        fixtures.beat(SingleInstanceProfile.REGION)
        workerMetrics.refresh()

        val body =
            given()
                .port(managementPort.toInt())
                .get("/q/metrics")
                .then()
                .statusCode(200)
                .extract()
                .asString()

        listOf(
            "uptime_queue_tasks{",
            "uptime_probes{",
            "uptime_queue_lag_seconds{",
            "uptime_worker_heartbeat_age_seconds{",
            "uptime_build_info{",
            "uptime_probe_expected_checks_per_minute ",
            "uptime_process_start_time_seconds ",
        ).forEach { series -> assertTrue(body.contains(series), "$series missing from /q/metrics") }
    }

    @Test
    fun `the metrics are not served on the public port`() {
        val status =
            given()
                .port(httpPort.toInt())
                .get("/q/metrics")
                .then()
                .extract()
                .statusCode()

        assertNotEquals(200, status)
    }

    private fun successfulTcpChecks(): Double =
        registry
            .find(ProbeCheckMetrics.CHECKS)
            .tags("protocol", "tcp", "result", "success")
            .counter()
            ?.count() ?: 0.0
}
