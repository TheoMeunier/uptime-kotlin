package tmenier.fr.schedulers.services

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import java.net.URI
import java.sql.DriverManager
import java.util.Properties

fun interface SqlServerHealthCheck {
    fun check(
        content: ProbeContent.SqlServer,
        timeoutSeconds: Int,
    )
}

@ApplicationScoped
class JdbcSqlServerHealthCheck : SqlServerHealthCheck {
    override fun check(
        content: ProbeContent.SqlServer,
        timeoutSeconds: Int,
    ) {
        val connection = parseConnectionString(content.connectionString)
        val properties =
            Properties().apply {
                connection.username?.let { setProperty("user", it) }
                connection.password?.let { setProperty("password", it) }
                setProperty("loginTimeout", timeoutSeconds.toString())
                setProperty("socketTimeout", (timeoutSeconds * 1_000).toString())
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

    internal fun parseConnectionString(connectionString: String): SqlServerConnection {
        val uri = URI(connectionString)
        require(uri.scheme == "sqlserver" || uri.scheme == "mssql") {
            "Connection string must start with sqlserver:// or mssql://"
        }
        require(!uri.host.isNullOrBlank()) { "Microsoft SQL Server host is required" }
        require(uri.path.isNotBlank() && uri.path != "/") {
            "Microsoft SQL Server database is required"
        }

        val credentials = uri.userInfo?.split(":", limit = 2)
        val port = if (uri.port == -1) 1433 else uri.port
        val host = if (':' in uri.host) "[${uri.host}]" else uri.host
        val database = uri.path.removePrefix("/")
        val parameters =
            uri.rawQuery
                ?.split("&")
                ?.filter { it.isNotBlank() }
                ?.joinToString(separator = ";", prefix = ";") { it }
                .orEmpty()

        return SqlServerConnection(
            jdbcUrl = "jdbc:sqlserver://$host:$port;databaseName=$database$parameters",
            username = credentials?.firstOrNull()?.takeIf { it.isNotBlank() },
            password = credentials?.getOrNull(1),
        )
    }
}

@RegisterForReflection
data class SqlServerConnection(
    val jdbcUrl: String,
    val username: String?,
    val password: String?,
)
