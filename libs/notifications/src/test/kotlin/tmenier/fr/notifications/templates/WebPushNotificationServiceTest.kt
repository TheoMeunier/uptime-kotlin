package tmenier.fr.notifications.templates

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.dtos.WebPushSubscription
import tmenier.fr.notifications.NotificationDateFormatter
import tmenier.fr.notifications.NotificationHttpClient
import tmenier.fr.notifications.webpush.WebPushCrypto
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.security.KeyPair
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.time.Duration
import java.time.Instant
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class WebPushNotificationServiceTest {
    private lateinit var server: HttpServer
    private val http = NotificationHttpClient(Duration.ofSeconds(2), Duration.ofSeconds(2))
    private val encryption = EncryptionService("0123456789abcdef0123456789abcdef")
    private val vapidKeys: KeyPair = WebPushCrypto.generateKeyPair()
    private val service =
        WebPushNotificationService(http, NotificationDateFormatter("UTC"), encryption, { vapidKeys }, { forgotten += it }, "mailto:ops@example.com")

    // The browser side of the subscription.
    private val browserKeys: KeyPair = WebPushCrypto.generateKeyPair()
    private val authSecret = ByteArray(16) { (it * 7).toByte() }

    private val statusByPath = mutableMapOf<String, Int>()
    private val bodies = mutableMapOf<String, ByteArray>()
    private val forgotten = mutableListOf<String>()
    private lateinit var baseUrl: String
    private var headers: Map<String, String> = emptyMap()
    private val body: ByteArray get() = bodies.values.last()

    @BeforeEach
    fun start() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            headers = exchange.requestHeaders.entries.associate { it.key.lowercase() to it.value.first() }
            bodies[exchange.requestURI.path] = exchange.requestBody.readBytes()
            exchange.sendResponseHeaders(statusByPath[exchange.requestURI.path] ?: 201, -1)
            exchange.close()
        }
        server.start()
        baseUrl = "http://127.0.0.1:${server.address.port}"
    }

    @AfterEach
    fun stop() {
        server.stop(0)
        http.close()
    }

    private fun browser(path: String) =
        WebPushSubscription(
            endpoint = "$baseUrl$path",
            p256dh = WebPushCrypto.base64Url(WebPushCrypto.rawPublicKey(browserKeys.public as ECPublicKey)),
            auth = encryption.encrypt(WebPushCrypto.base64Url(authSecret)),
            label = "Firefox · macOS",
        )

    private fun subscription(vararg paths: String = arrayOf("/push/abc123")) = NotificationContent.WebPush(paths.map(::browser))

    @Test
    fun `posts an aes128gcm payload that the browser can decrypt`() {
        service.sendTest(subscription())

        assertEquals("aes128gcm", headers["content-encoding"])
        assertEquals("application/octet-stream", headers["content-type"])
        assertEquals("86400", headers["ttl"])
        assertEquals("normal", headers["urgency"])

        val json = decrypt(body)
        assertTrue(json.contains("\"title\":\"Test notification\""), json)
        assertTrue(json.contains("\"url\":\"/\""), json)
    }

    @Test
    fun `signs the request with a VAPID token for the push service origin`() {
        service.sendTest(subscription())

        val authorization = headers.getValue("authorization")
        val match = Regex("^vapid t=([^.]+)\\.([^.]+)\\.([^,]+), k=(.+)$").find(authorization)
        checkNotNull(match) { authorization }
        val (header, claims, signature, key) = match.destructured

        assertEquals(WebPushCrypto.base64Url(WebPushCrypto.rawPublicKey(vapidKeys.public as ECPublicKey)), key)
        val decodedClaims = String(WebPushCrypto.decodeBase64(claims))
        assertTrue(decodedClaims.contains("\"aud\":\"$baseUrl\""), decodedClaims)
        assertTrue(decodedClaims.contains("\"sub\":\"mailto:ops@example.com\""), decodedClaims)

        val valid =
            Signature.getInstance("SHA256withECDSAinP1363Format").run {
                initVerify(WebPushCrypto.publicKeyFromRaw(WebPushCrypto.decodeBase64(key)))
                update("$header.$claims".toByteArray())
                verify(WebPushCrypto.decodeBase64(signature))
            }
        assertTrue(valid)
    }

    @Test
    fun `sends to every browser of the channel`() {
        service.sendTest(subscription("/push/firefox", "/push/safari"))

        assertEquals(setOf("/push/firefox", "/push/safari"), bodies.keys)
        assertTrue(forgotten.isEmpty())
    }

    @Test
    fun `forgets an expired browser and still succeeds for the others`() {
        statusByPath["/push/gone"] = 410

        service.sendTest(subscription("/push/gone", "/push/alive"))

        assertEquals(listOf("$baseUrl/push/gone"), forgotten)
        assertTrue(bodies.containsKey("/push/alive"))
    }

    @Test
    fun `fails clearly when every browser has expired`() {
        statusByPath["/push/gone"] = 404

        val error = assertThrows<IllegalStateException> { service.sendTest(subscription("/push/gone")) }

        assertTrue(error.message!!.contains("add the browser again"), error.message)
        assertEquals(listOf("$baseUrl/push/gone"), forgotten)
    }

    @Test
    fun `fails on any other error from the push service`() {
        statusByPath["/push/abc123"] = 403

        val error = assertThrows<IllegalStateException> { service.sendTest(subscription()) }

        assertTrue(error.message!!.contains("HTTP 403"), error.message)
        assertTrue(forgotten.isEmpty())
    }

    @Test
    fun `succeeds when at least one browser got the notification`() {
        statusByPath["/push/broken"] = 500

        service.sendTest(subscription("/push/broken", "/push/ok"))

        assertTrue(forgotten.isEmpty())
    }

    @Test
    fun `refuses a channel without browsers`() {
        assertThrows<IllegalStateException> { service.sendTest(NotificationContent.WebPush(emptyList())) }
    }

    @Test
    fun `keeps the payload under the push service limit`() {
        val result = ProbeResult(ProbeMonitorLogStatus.FAILURE, 0, "é".repeat(10_000), Instant.parse("2026-10-06T14:32:05Z"))
        val json = service.buildPayload("x".repeat(1000), result, tag = "t", url = "/", urgent = true)

        assertTrue(json.toByteArray().size < 3900, "${json.toByteArray().size} bytes")
        assertTrue(json.contains("\"require_interaction\":true"))
    }

    @Test
    fun `accepts subscription keys in padded base64`() {
        val padded = WebPushCrypto.base64Url(authSecret).replace('-', '+').replace('_', '/') + "=="

        assertArrayEquals(authSecret, WebPushCrypto.decodeBase64(padded))
    }

    /** What the browser does on reception (RFC 8291 §3.4), written from the receiver's side. */
    private fun decrypt(payload: ByteArray): String {
        val buffer = ByteBuffer.wrap(payload)
        val salt = ByteArray(16).also(buffer::get)
        val recordSize = buffer.int
        val senderPublic = ByteArray(buffer.get().toInt()).also(buffer::get)
        val ciphertext = ByteArray(buffer.remaining()).also(buffer::get)
        assertEquals(4096, recordSize)

        val ecdh =
            KeyAgreement.getInstance("ECDH").run {
                init(browserKeys.private)
                doPhase(WebPushCrypto.publicKeyFromRaw(senderPublic), true)
                generateSecret()
            }
        val browserPublic = WebPushCrypto.rawPublicKey(browserKeys.public as ECPublicKey)
        val ikm = hmac(hmac(authSecret, ecdh), "WebPush: info".toByteArray() + byteArrayOf(0) + browserPublic + senderPublic + byteArrayOf(1))
        val prk = hmac(salt, ikm)
        val cek = hmac(prk, "Content-Encoding: aes128gcm".toByteArray() + byteArrayOf(0, 1)).copyOf(16)
        val nonce = hmac(prk, "Content-Encoding: nonce".toByteArray() + byteArrayOf(0, 1)).copyOf(12)

        val plaintext =
            Cipher.getInstance("AES/GCM/NoPadding").run {
                init(Cipher.DECRYPT_MODE, SecretKeySpec(cek, "AES"), GCMParameterSpec(128, nonce))
                doFinal(ciphertext)
            }
        assertEquals(2.toByte(), plaintext.last(), "last record delimiter")
        return String(plaintext.copyOf(plaintext.size - 1))
    }

    private fun hmac(
        key: ByteArray,
        data: ByteArray,
    ): ByteArray =
        Mac.getInstance("HmacSHA256").run {
            init(SecretKeySpec(key, "HmacSHA256"))
            doFinal(data)
        }
}
