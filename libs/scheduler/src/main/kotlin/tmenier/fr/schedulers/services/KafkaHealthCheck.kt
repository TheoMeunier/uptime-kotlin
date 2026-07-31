package tmenier.fr.schedulers.services

import jakarta.enterprise.context.ApplicationScoped
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.DescribeTopicsOptions
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import tmenier.fr.common.dtos.ProbeContent
import java.time.Duration
import java.util.Properties
import java.util.concurrent.TimeUnit

fun interface KafkaHealthCheck {
    fun check(
        content: ProbeContent.Kafka,
        timeoutSeconds: Int,
    )
}

@ApplicationScoped
class ProducerKafkaHealthCheck : KafkaHealthCheck {
    override fun check(
        content: ProbeContent.Kafka,
        timeoutSeconds: Int,
    ) {
        if (!content.allowAutoTopicCreation) {
            requireTopicExists(content, timeoutSeconds)
        }

        val producer = KafkaProducer<String, String>(buildProperties(content, timeoutSeconds))
        try {
            producer
                .send(ProducerRecord(content.topic, content.message))
                .get(timeoutSeconds.toLong() + 2, TimeUnit.SECONDS)
        } finally {
            producer.close(Duration.ofSeconds(1))
        }
    }

    internal fun buildProperties(
        content: ProbeContent.Kafka,
        timeoutSeconds: Int,
    ): Properties {
        require(content.brokers.isNotBlank()) { "At least one Kafka broker is required" }
        require(content.topic.isNotBlank()) { "Kafka topic is required" }

        val timeoutMilliseconds = timeoutSeconds * 1_000
        return Properties().apply {
            setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, content.brokers)
            setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            setProperty(ProducerConfig.CLIENT_ID_CONFIG, "uptime-kotlin-monitor")
            setProperty(ProducerConfig.ACKS_CONFIG, "all")
            setProperty(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, timeoutMilliseconds.toString())
            setProperty(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, (timeoutMilliseconds + 1_000).toString())
            setProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG, timeoutMilliseconds.toString())
            setProperty(
                CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
                if (content.ssl) "SSL" else "PLAINTEXT",
            )
        }
    }

    internal fun buildAdminProperties(
        content: ProbeContent.Kafka,
        timeoutSeconds: Int,
    ): Properties {
        val timeoutMilliseconds = timeoutSeconds * 1_000
        return Properties().apply {
            setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, content.brokers)
            setProperty(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, timeoutMilliseconds.toString())
            setProperty(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, timeoutMilliseconds.toString())
            setProperty(
                CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
                if (content.ssl) "SSL" else "PLAINTEXT",
            )
        }
    }

    private fun requireTopicExists(
        content: ProbeContent.Kafka,
        timeoutSeconds: Int,
    ) {
        val timeoutMilliseconds = timeoutSeconds * 1_000
        val admin = AdminClient.create(buildAdminProperties(content, timeoutSeconds))
        try {
            admin
                .describeTopics(
                    listOf(content.topic),
                    DescribeTopicsOptions().timeoutMs(timeoutMilliseconds),
                ).allTopicNames()
                .get(timeoutSeconds.toLong(), TimeUnit.SECONDS)
        } finally {
            admin.close(Duration.ofSeconds(1))
        }
    }
}
