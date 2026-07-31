package tmenier.fr.schedulers.templates

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.services.KafkaHealthCheck
import tmenier.fr.schedulers.services.ProducerKafkaHealthCheck
import java.time.LocalDateTime
import java.util.UUID

class ProbeProtocolKafkaTest {
    @Test
    fun `configures all Kafka brokers and SSL`() {
        val properties =
            ProducerKafkaHealthCheck().buildProperties(
                content().copy(ssl = true, allowAutoTopicCreation = true),
                timeoutSeconds = 5,
            )

        assertEquals(
            "kafka-1:9092,kafka-2:9092",
            properties.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG),
        )
        assertEquals("SSL", properties.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG))
        assertEquals("all", properties.getProperty(ProducerConfig.ACKS_CONFIG))
    }

    @Test
    fun `returns success when Kafka acknowledges the message`() {
        val executor = ProbeProtocolKafka(KafkaHealthCheck { _, _ -> })

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, result.status)
        assertTrue(result.message.startsWith("Kafka message produced successfully"))
    }

    @Test
    fun `returns warning when a retry remains`() {
        val executor =
            ProbeProtocolKafka(
                KafkaHealthCheck { _, _ -> error("broker unavailable") },
            )

        val result = executor.execute(probe(), content(), false)

        assertEquals(ProbeMonitorLogStatus.WARNING, result.status)
        assertTrue(result.message.contains("broker unavailable"))
    }

    @Test
    fun `returns failure on the last attempt`() {
        val executor =
            ProbeProtocolKafka(
                KafkaHealthCheck { _, _ -> error("topic not found") },
            )

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.FAILURE, result.status)
        assertTrue(result.message.contains("topic not found"))
    }

    private fun content() =
        ProbeContent.Kafka(
            brokers = "kafka-1:9092,kafka-2:9092",
            topic = "health-check",
            message = "uptime-kotlin",
            ssl = false,
            allowAutoTopicCreation = false,
        )

    private fun probe() =
        ProbeDTO(
            id = UUID.randomUUID(),
            name = "Kafka test",
            interval = 60,
            timeout = 5,
            retry = 1,
            intervalRetry = 1,
            enabled = true,
            protocol = ProbeProtocol.KAFKA,
            description = null,
            lastRun = null,
            status = ProbeMonitorLogStatus.SUCCESS,
            content = content(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
}
