package tmenier.fr.common.enums.monitors

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class ProbeProtocol(
    @get:JsonValue val value: String,
) {
    HTTP("HTTP"),
    TCP("TCP"),
    PING("PING"),
    DNS("DNS"),
    POSTGRESQL("POSTGRESQL"),
    SQLSERVER("MICROSOFT SQL SERVER"),
    MYSQL("MYSQL / MARIADB"),
    REDIS("REDIS"),
    ;

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromValue(value: String): ProbeProtocol =
            entries.firstOrNull { it.value == value || it.name == value }
                ?: throw IllegalArgumentException("Unknown probe protocol: $value")
    }
}
