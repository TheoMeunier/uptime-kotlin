package tmenier.fr.schedulers.templates

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.common.enums.monitors.SmtpSecurity
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.services.SmtpHealthCheck
import tmenier.fr.schedulers.services.SocketSmtpHealthCheck
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ProbeProtocolSmtpTest {
    @Test
    fun `checks the SMTP greeting and EHLO without sending an email`() {
        ServerSocket(0).use { server ->
            val executor = Executors.newSingleThreadExecutor()
            try {
                val serverResult =
                    executor.submit<String> {
                        server.accept().use { socket ->
                            val reader =
                                BufferedReader(
                                    InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII),
                                )
                            val writer =
                                BufferedWriter(
                                    OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII),
                                )
                            writer.write("220 smtp.example.com ESMTP ready\r\n")
                            writer.flush()
                            val command = reader.readLine()
                            writer.write("250-smtp.example.com\r\n250 PIPELINING\r\n")
                            writer.flush()
                            command
                        }
                    }

                SocketSmtpHealthCheck().check(
                    ProbeContent.Smtp(
                        hostname = "127.0.0.1",
                        port = server.localPort,
                        security = SmtpSecurity.IGNORE_TLS,
                    ),
                    timeoutSeconds = 2,
                )

                assertEquals("EHLO uptime-kotlin", serverResult.get(2, TimeUnit.SECONDS))
            } finally {
                executor.shutdownNow()
            }
        }
    }

    @Test
    fun `returns success when SMTP accepts the connection`() {
        val executor = ProbeProtocolSmtp(SmtpHealthCheck { _, _ -> })

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, result.status)
        assertTrue(result.message.startsWith("SMTP connection successful"))
    }

    @Test
    fun `returns warning when a retry remains`() {
        val executor =
            ProbeProtocolSmtp(
                SmtpHealthCheck { _, _ -> error("connection refused") },
            )

        val result = executor.execute(probe(), content(), false)

        assertEquals(ProbeMonitorLogStatus.WARNING, result.status)
        assertTrue(result.message.contains("connection refused"))
    }

    @Test
    fun `returns failure on the last attempt`() {
        val executor =
            ProbeProtocolSmtp(
                SmtpHealthCheck { _, _ -> error("certificate expired") },
            )

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.FAILURE, result.status)
        assertTrue(result.message.contains("certificate expired"))
    }

    private fun content() =
        ProbeContent.Smtp(
            hostname = "smtp.example.com",
            port = 587,
            security = SmtpSecurity.STARTTLS,
        )

    private fun probe() =
        ProbeDTO(
            id = UUID.randomUUID(),
            name = "SMTP test",
            interval = 60,
            timeout = 5,
            retry = 1,
            intervalRetry = 1,
            enabled = true,
            protocol = ProbeProtocol.SMTP,
            description = null,
            lastRun = null,
            status = ProbeMonitorLogStatus.SUCCESS,
            content = content(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
}
