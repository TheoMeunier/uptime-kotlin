package tmenier.fr.schedulers.templates

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.services.DockerContainerInspector
import tmenier.fr.schedulers.services.DockerContainerState
import java.time.Duration
import java.time.Instant
import java.util.UUID

class ProbeProtocolDockerContainerTest {
    private val executor = ProbeProtocolDockerContainer(DockerContainerInspector { _, _ -> error("unused") })

    @Test
    fun `is up when the container runs without health check or is healthy`() {
        assertNull(executor.problemOf(state("running")))
        assertNull(executor.problemOf(state("running", health = "healthy")))
    }

    @Test
    fun `describes every unsuccessful state`() {
        assertEquals(
            "Container web is running but its health check is still starting",
            executor.problemOf(state("running", health = "starting")),
        )
        assertEquals("Container web is unhealthy", executor.problemOf(state("running", health = "unhealthy")))
        assertEquals("Container web is exited (exit code 137)", executor.problemOf(state("exited", exitCode = 137)))
        assertEquals(
            "Container web is dead (exit code 1, OCI runtime error)",
            executor.problemOf(state("dead", exitCode = 1, error = "OCI runtime error")),
        )
        assertEquals("Container web is restarting (restarted 4 times)", executor.problemOf(state("restarting", restartCount = 4)))
        assertEquals("Container web is paused", executor.problemOf(state("paused")))
    }

    @Test
    fun `returns success with the health status`() {
        val result = run(state("running", health = "healthy"), isLastAttempt = true)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, result.status)
        assertEquals("Container web is running (healthy)", result.message)
    }

    @Test
    fun `returns warning when a retry remains`() {
        val result = run(state("exited", exitCode = 1), isLastAttempt = false)

        assertEquals(ProbeMonitorLogStatus.WARNING, result.status)
    }

    @Test
    fun `returns failure on the last attempt`() {
        val result = run(state("running", health = "unhealthy"), isLastAttempt = true)

        assertEquals(ProbeMonitorLogStatus.FAILURE, result.status)
        assertEquals("Container web is unhealthy", result.message)
    }

    @Test
    fun `returns failure when the Docker Engine cannot be reached`() {
        val failing =
            ProbeProtocolDockerContainer(
                DockerContainerInspector { _, _ -> error("cannot reach the Docker socket /var/run/docker.sock: Permission denied") },
            )

        val result = failing.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.FAILURE, result.status)
        assertTrue(result.message.startsWith("Docker container check failed: cannot reach the Docker socket"), result.message)
    }

    @Test
    fun `passes the probe timeout to the inspector`() {
        var received: Duration? = null
        val executor =
            ProbeProtocolDockerContainer(
                DockerContainerInspector { _, timeout ->
                    received = timeout
                    state("running")
                },
            )

        executor.execute(probe(timeout = 12), content(), true)

        assertEquals(Duration.ofSeconds(12), received)
    }

    private fun run(
        state: DockerContainerState,
        isLastAttempt: Boolean,
    ) = ProbeProtocolDockerContainer(DockerContainerInspector { _, _ -> state }).execute(probe(), content(), isLastAttempt)

    private fun state(
        status: String,
        health: String? = null,
        exitCode: Int? = null,
        restartCount: Int = 0,
        error: String? = null,
    ) = DockerContainerState("web", status, health, exitCode, restartCount, error)

    private fun content() = ProbeContent.DockerContainer(containerName = "web")

    private fun probe(timeout: Int = 5) =
        ProbeDTO(
            id = UUID.randomUUID(),
            name = "Docker test",
            interval = 60,
            timeout = timeout,
            retry = 1,
            intervalRetry = 1,
            enabled = true,
            protocol = ProbeProtocol.DOCKER,
            description = null,
            lastRun = null,
            status = ProbeMonitorLogStatus.SUCCESS,
            content = content(),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
}
