package tmenier.fr.schedulers.services

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.enums.monitors.SmtpSecurity
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.EOFException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

fun interface SmtpHealthCheck {
    fun check(
        content: ProbeContent.Smtp,
        timeoutSeconds: Int,
    )
}

@ApplicationScoped
class SocketSmtpHealthCheck : SmtpHealthCheck {
    override fun check(
        content: ProbeContent.Smtp,
        timeoutSeconds: Int,
    ) {
        val timeoutMilliseconds = timeoutSeconds * 1_000

        when (content.security) {
            SmtpSecurity.SMTPS ->
                createTlsSocket(content.hostname, content.port, timeoutMilliseconds).use { socket ->
                    checkGreetingAndEhlo(socket)
                }

            SmtpSecurity.STARTTLS ->
                connect(content.hostname, content.port, timeoutMilliseconds).use { socket ->
                    val session = SmtpSession(socket)
                    session.expect(220, "SMTP greeting")
                    session.command("EHLO $CLIENT_NAME", 250)
                    session.command("STARTTLS", 220)

                    upgradeToTls(socket, content.hostname, content.port, timeoutMilliseconds).use { tlsSocket ->
                        SmtpSession(tlsSocket).command("EHLO $CLIENT_NAME", 250)
                    }
                }

            SmtpSecurity.IGNORE_TLS ->
                connect(content.hostname, content.port, timeoutMilliseconds).use { socket ->
                    checkGreetingAndEhlo(socket)
                }
        }
    }

    private fun checkGreetingAndEhlo(socket: Socket) {
        val session = SmtpSession(socket)
        session.expect(220, "SMTP greeting")
        session.command("EHLO $CLIENT_NAME", 250)
    }

    private fun connect(
        hostname: String,
        port: Int,
        timeoutMilliseconds: Int,
    ): Socket =
        Socket().apply {
            connect(InetSocketAddress(hostname, port), timeoutMilliseconds)
            soTimeout = timeoutMilliseconds
        }

    private fun createTlsSocket(
        hostname: String,
        port: Int,
        timeoutMilliseconds: Int,
    ): SSLSocket {
        val socket = SSLSocketFactory.getDefault().createSocket() as SSLSocket
        return try {
            socket.connect(InetSocketAddress(hostname, port), timeoutMilliseconds)
            socket.soTimeout = timeoutMilliseconds
            configureTls(socket)
            socket.startHandshake()
            socket
        } catch (error: Exception) {
            socket.close()
            throw error
        }
    }

    private fun upgradeToTls(
        socket: Socket,
        hostname: String,
        port: Int,
        timeoutMilliseconds: Int,
    ): SSLSocket {
        val socketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        val tlsSocket =
            socketFactory
                .createSocket(socket, hostname, port, true) as SSLSocket
        return try {
            tlsSocket.soTimeout = timeoutMilliseconds
            configureTls(tlsSocket)
            tlsSocket.startHandshake()
            tlsSocket
        } catch (error: Exception) {
            tlsSocket.close()
            throw error
        }
    }

    private fun configureTls(socket: SSLSocket) {
        socket.sslParameters =
            socket.sslParameters.apply {
                endpointIdentificationAlgorithm = "HTTPS"
            }
    }

    private companion object {
        const val CLIENT_NAME = "uptime-kotlin"
    }
}

internal class SmtpSession(
    socket: Socket,
) {
    private val reader =
        BufferedReader(
            InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII),
        )
    private val writer =
        BufferedWriter(
            OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII),
        )

    fun command(
        command: String,
        expectedCode: Int,
    ): SmtpResponse {
        writer.write(command)
        writer.write("\r\n")
        writer.flush()
        return expect(expectedCode, command.substringBefore(' '))
    }

    fun expect(
        expectedCode: Int,
        operation: String,
    ): SmtpResponse {
        val response = readResponse()
        require(response.code == expectedCode) {
            "$operation failed: ${response.lines.joinToString(" ")}"
        }
        return response
    }

    internal fun readResponse(): SmtpResponse {
        val firstLine = reader.readLine() ?: throw EOFException("SMTP server closed the connection")
        require(firstLine.length >= 3) { "Invalid SMTP response: $firstLine" }
        val code =
            requireNotNull(firstLine.take(3).toIntOrNull()) {
                "Invalid SMTP response: $firstLine"
            }
        val lines = mutableListOf(firstLine)
        var currentLine = firstLine

        while (currentLine.getOrNull(3) == '-') {
            currentLine = reader.readLine() ?: throw EOFException("Incomplete SMTP response")
            lines += currentLine
        }

        return SmtpResponse(code, lines)
    }
}

@RegisterForReflection
internal data class SmtpResponse(
    val code: Int,
    val lines: List<String>,
)
