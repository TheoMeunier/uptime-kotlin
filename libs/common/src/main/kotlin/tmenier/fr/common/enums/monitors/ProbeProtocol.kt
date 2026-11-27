package tmenier.fr.common.enums.monitors

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import tmenier.fr.common.dtos.ProbeContent

/**
 * The ordinal is persisted: add new protocols at the end, never reorder.
 *
 * [contentClass] is the [ProbeContent] stored in the probe's JSON column for this protocol.
 */
enum class ProbeProtocol(
    @get:JsonValue val value: String,
    val contentClass: Class<out ProbeContent>,
) {
    HTTP("HTTP", ProbeContent.Http::class.java),
    TCP("TCP", ProbeContent.Tcp::class.java),
    PING("PING", ProbeContent.Ping::class.java),
    DNS("DNS", ProbeContent.Dns::class.java),
    POSTGRESQL("POSTGRESQL", ProbeContent.PostgreSql::class.java),
    SQLSERVER("MICROSOFT SQL SERVER", ProbeContent.SqlServer::class.java),
    MYSQL("MYSQL / MARIADB", ProbeContent.MySql::class.java),
    REDIS("REDIS", ProbeContent.Redis::class.java),
    SMTP("SMTP", ProbeContent.Smtp::class.java),
    KAFKA("KAFKA PRODUCER", ProbeContent.Kafka::class.java),
    RABBITMQ("RABBITMQ", ProbeContent.RabbitMq::class.java),
    MONGODB("MONGODB", ProbeContent.MongoDb::class.java),
    ORACLE("ORACLE", ProbeContent.Oracle::class.java),
    WEBSOCKET("WEBSOCKET UPGRADE", ProbeContent.WebSocketUpgrade::class.java),
    DOCKER("DOCKER CONTAINER", ProbeContent.DockerContainer::class.java),
    PUSH("PUSH", ProbeContent.Push::class.java),
    ;

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromValue(value: String): ProbeProtocol =
            entries.firstOrNull { it.value == value || it.name == value }
                ?: throw IllegalArgumentException("Unknown probe protocol: $value")

        fun of(content: ProbeContent): ProbeProtocol = entries.first { it.contentClass.isInstance(content) }
    }
}
