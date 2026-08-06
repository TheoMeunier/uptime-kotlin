package tmenier.fr.schedulers.services

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.encryption.EncryptionService
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.EOFException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import java.nio.charset.StandardCharsets
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

fun interface RedisHealthCheck {
    fun check(
        content: ProbeContent.Redis,
        timeoutSeconds: Int,
    )
}

@ApplicationScoped
class SocketRedisHealthCheck(
    private val encryptionService: EncryptionService,
) : RedisHealthCheck {
    override fun check(
        content: ProbeContent.Redis,
        timeoutSeconds: Int,
    ) {
        val connection = parseConnectionString(content.connectionString)
        val timeoutMilliseconds = timeoutSeconds * 1_000

        createSocket(connection.tls).use { socket ->
            socket.connect(InetSocketAddress(connection.host, connection.port), timeoutMilliseconds)
            socket.soTimeout = timeoutMilliseconds
            if (socket is SSLSocket) socket.startHandshake()

            val input = BufferedInputStream(socket.getInputStream())
            val output = BufferedOutputStream(socket.getOutputStream())

            authenticate(connection, input, output)
            if (connection.database != 0) {
                execute(listOf("SELECT", connection.database.toString()), input, output)
            }
            execute(parseCommand(content.command), input, output)
        }
    }

    internal fun parseConnectionString(connectionString: String): RedisConnection {
        val uri = URI(encryptionService.decryptIfEncrypted(connectionString))
        require(uri.scheme == "redis" || uri.scheme == "rediss") {
            "Connection string must start with redis:// or rediss://"
        }
        require(!uri.host.isNullOrBlank()) { "Redis host is required" }

        val credentials = parseCredentials(uri.userInfo)
        val databasePath = uri.path.removePrefix("/")
        val database =
            if (databasePath.isBlank()) {
                0
            } else {
                requireNotNull(databasePath.toIntOrNull()) {
                    "Redis database must be a positive integer"
                }
            }
        require(database >= 0) { "Redis database must be a positive integer" }

        return RedisConnection(
            host = uri.host,
            port = if (uri.port == -1) 6379 else uri.port,
            username = credentials?.first,
            password = credentials?.second,
            database = database,
            tls = uri.scheme == "rediss",
        )
    }

    internal fun parseCommand(command: String): List<String> {
        val arguments = mutableListOf<String>()
        val current = StringBuilder()
        var quote: Char? = null
        var escaped = false
        var tokenStarted = false

        command.forEach { character ->
            when {
                escaped -> {
                    current.append(character)
                    escaped = false
                    tokenStarted = true
                }

                character == '\\' -> {
                    escaped = true
                    tokenStarted = true
                }

                quote != null && character == quote -> quote = null
                quote != null -> current.append(character)
                character == '"' || character == '\'' -> {
                    quote = character
                    tokenStarted = true
                }

                character.isWhitespace() -> {
                    if (tokenStarted) {
                        arguments += current.toString()
                        current.clear()
                        tokenStarted = false
                    }
                }

                else -> {
                    current.append(character)
                    tokenStarted = true
                }
            }
        }

        require(!escaped) { "Redis command cannot end with an escape character" }
        require(quote == null) { "Redis command contains an unclosed quote" }
        if (tokenStarted) arguments += current.toString()
        require(arguments.isNotEmpty()) { "Redis command is required" }
        return arguments
    }

    private fun parseCredentials(userInfo: String?): Pair<String?, String?>? {
        if (userInfo == null) return null
        val parts = userInfo.split(":", limit = 2)
        return if (parts.size == 1) {
            null to parts[0]
        } else {
            parts[0].takeIf { it.isNotBlank() } to parts[1]
        }
    }

    private fun createSocket(tls: Boolean): Socket =
        if (tls) {
            SSLSocketFactory.getDefault().createSocket()
        } else {
            Socket()
        }

    private fun authenticate(
        connection: RedisConnection,
        input: BufferedInputStream,
        output: BufferedOutputStream,
    ) {
        val password = connection.password ?: return
        val command =
            connection.username?.let { listOf("AUTH", it, password) }
                ?: listOf("AUTH", password)
        execute(command, input, output)
    }

    private fun execute(
        arguments: List<String>,
        input: BufferedInputStream,
        output: BufferedOutputStream,
    ) {
        writeCommand(arguments, output)
        readResponse(input)
    }

    private fun writeCommand(
        arguments: List<String>,
        output: BufferedOutputStream,
    ) {
        output.write("*${arguments.size}\r\n".toByteArray(StandardCharsets.UTF_8))
        arguments.forEach { argument ->
            val bytes = argument.toByteArray(StandardCharsets.UTF_8)
            output.write("$${bytes.size}\r\n".toByteArray(StandardCharsets.UTF_8))
            output.write(bytes)
            output.write(CRLF)
        }
        output.flush()
    }

    private fun readResponse(input: BufferedInputStream) {
        when (val type = input.read()) {
            -1 -> throw EOFException("Redis closed the connection")
            '+'.code, ':'.code, ','.code, '#'.code, '_'.code -> readLine(input)
            '-'.code -> throw IllegalStateException("Redis error: ${readLine(input)}")
            '$'.code, '='.code -> readBulk(input)
            '!'.code -> throw IllegalStateException("Redis error: ${readBulk(input)}")
            '*'.code, '~'.code, '>'.code -> repeat(readCount(input)) { readResponse(input) }
            '%'.code, '|'.code -> repeat(readCount(input) * 2) { readResponse(input) }
            else -> throw IllegalStateException("Unsupported Redis response type: ${type.toChar()}")
        }
    }

    private fun readCount(input: BufferedInputStream): Int {
        val count = readLine(input).toInt()
        return if (count < 0) 0 else count
    }

    private fun readBulk(input: BufferedInputStream): String {
        val length = readLine(input).toInt()
        if (length < 0) return ""
        val bytes = input.readNBytes(length)
        if (bytes.size != length) throw EOFException("Incomplete Redis response")
        require(input.read() == '\r'.code && input.read() == '\n'.code) {
            "Invalid Redis response"
        }
        return String(bytes, StandardCharsets.UTF_8)
    }

    private fun readLine(input: BufferedInputStream): String {
        val bytes = mutableListOf<Byte>()
        while (true) {
            val value = input.read()
            if (value == -1) throw EOFException("Incomplete Redis response")
            if (value == '\r'.code) {
                require(input.read() == '\n'.code) { "Invalid Redis response" }
                return bytes.toByteArray().toString(StandardCharsets.UTF_8)
            }
            bytes += value.toByte()
        }
    }

    private companion object {
        val CRLF = byteArrayOf('\r'.code.toByte(), '\n'.code.toByte())
    }
}

@RegisterForReflection
data class RedisConnection(
    val host: String,
    val port: Int,
    val username: String?,
    val password: String?,
    val database: Int,
    val tls: Boolean,
)
