package tmenier.fr.schedulers.services

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.encryption.EncryptionService
import java.net.URI
import java.sql.DriverManager
import java.util.Properties

fun interface MySqlHealthCheck {
    fun check(
        content: ProbeContent.MySql,
        timeoutSeconds: Int,
    )
}

@ApplicationScoped
class JdbcMySqlHealthCheck(
    private val encryptionService: EncryptionService,
) : MySqlHealthCheck {
    override fun check(
        content: ProbeContent.MySql,
        timeoutSeconds: Int,
    ) {
        val connection = parseConnectionString(content.connectionString)
        val timeoutMilliseconds = timeoutSeconds * 1_000
        val properties =
            Properties().apply {
                connection.username?.let { setProperty("user", it) }
                connection.password?.let { setProperty("password", it) }
                setProperty("connectTimeout", timeoutMilliseconds.toString())
                setProperty("socketTimeout", timeoutMilliseconds.toString())
            }

        DriverManager
            .getConnection(connection.jdbcUrl, properties)
            .use { databaseConnection ->
                databaseConnection.createStatement().use { statement ->
                    statement.queryTimeout = timeoutSeconds
                    statement.execute(content.query)
                }
            }
    }

    internal fun parseConnectionString(connectionString: String): MySqlConnection {
        val uri = URI(encryptionService.decryptIfEncrypted(connectionString))
        require(uri.scheme == "mysql" || uri.scheme == "mariadb") {
            "Connection string must start with mysql:// or mariadb://"
        }
        require(!uri.host.isNullOrBlank()) { "MySQL/MariaDB host is required" }
        require(uri.path.isNotBlank() && uri.path != "/") { "MySQL/MariaDB database is required" }

        val credentials = uri.userInfo?.split(":", limit = 2)
        val port = if (uri.port == -1) 3306 else uri.port
        val host = if (':' in uri.host) "[${uri.host}]" else uri.host
        val parameters = uri.rawQuery?.let { "?$it" }.orEmpty()

        return MySqlConnection(
            jdbcUrl = "jdbc:${uri.scheme}://$host:$port${uri.rawPath}$parameters",
            username = credentials?.firstOrNull()?.takeIf { it.isNotBlank() },
            password = credentials?.getOrNull(1),
        )
    }
}

@RegisterForReflection
data class MySqlConnection(
    val jdbcUrl: String,
    val username: String?,
    val password: String?,
)
