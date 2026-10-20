package tmenier.fr.notifications.webpush

import java.math.BigInteger
import java.net.URI
import java.nio.ByteBuffer
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Duration
import java.time.Instant
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object WebPushCrypto {
    private const val CURVE = "secp256r1"
    private const val RECORD_SIZE = 4096
    private const val KEY_LENGTH = 16
    private const val NONCE_LENGTH = 12
    private const val SALT_LENGTH = 16
    private const val TAG_BITS = 128
    private const val COORDINATE_LENGTH = 32
    private const val LAST_RECORD_DELIMITER: Byte = 2
    private const val SEPARATOR: Byte = 0
    private const val FIRST_BLOCK: Byte = 1

    private val encoder = Base64.getUrlEncoder().withoutPadding()

    fun generateKeyPair(): KeyPair = KeyPairGenerator.getInstance("EC").apply { initialize(ECGenParameterSpec(CURVE), SecureRandom()) }.generateKeyPair()

    fun base64Url(bytes: ByteArray): String = encoder.encodeToString(bytes)

    fun decodeBase64(value: String): ByteArray =
        Base64.getUrlDecoder().decode(
            value
                .trim()
                .trimEnd('=')
                .replace('+', '-')
                .replace('/', '_'),
        )

    fun rawPublicKey(key: ECPublicKey): ByteArray = byteArrayOf(4) + unsigned(key.w.affineX) + unsigned(key.w.affineY)

    fun publicKeyFromRaw(raw: ByteArray): ECPublicKey {
        require(raw.size == 1 + 2 * COORDINATE_LENGTH && raw[0] == 4.toByte()) { "Invalid P-256 public key (expected 65 bytes, uncompressed)" }
        val point =
            ECPoint(
                BigInteger(1, raw.copyOfRange(1, 1 + COORDINATE_LENGTH)),
                BigInteger(1, raw.copyOfRange(1 + COORDINATE_LENGTH, raw.size)),
            )
        return KeyFactory.getInstance("EC").generatePublic(ECPublicKeySpec(point, curveParameters())) as ECPublicKey
    }

    fun privateKeyFromPkcs8(encoded: ByteArray): PrivateKey = KeyFactory.getInstance("EC").generatePrivate(PKCS8EncodedKeySpec(encoded))

    fun encrypt(
        plaintext: ByteArray,
        p256dh: ByteArray,
        auth: ByteArray,
        senderKeys: KeyPair = generateKeyPair(),
        salt: ByteArray = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) },
    ): ByteArray {
        require(auth.size == KEY_LENGTH) { "Invalid subscription secret (expected 16 bytes, got ${auth.size})" }
        require(plaintext.size + 1 + TAG_BITS / 8 <= RECORD_SIZE) { "Payload too large for a single record" }

        val userAgentPublic = publicKeyFromRaw(p256dh)
        val senderPublic = rawPublicKey(senderKeys.public as ECPublicKey)

        val ecdhSecret =
            KeyAgreement.getInstance("ECDH").run {
                init(senderKeys.private)
                doPhase(userAgentPublic, true)
                generateSecret()
            }

        val keyInfo = "WebPush: info".toByteArray() + SEPARATOR + p256dh + senderPublic
        val ikm = hkdf(salt = auth, ikm = ecdhSecret, info = keyInfo, length = 32)
        val prk = hmac(salt, ikm)
        val cek = expand(prk, "Content-Encoding: aes128gcm".toByteArray() + SEPARATOR, KEY_LENGTH)
        val nonce = expand(prk, "Content-Encoding: nonce".toByteArray() + SEPARATOR, NONCE_LENGTH)

        val ciphertext =
            Cipher.getInstance("AES/GCM/NoPadding").run {
                init(Cipher.ENCRYPT_MODE, SecretKeySpec(cek, "AES"), GCMParameterSpec(TAG_BITS, nonce))
                doFinal(plaintext + LAST_RECORD_DELIMITER)
            }

        return ByteBuffer
            .allocate(SALT_LENGTH + 4 + 1 + senderPublic.size + ciphertext.size)
            .put(salt)
            .putInt(RECORD_SIZE)
            .put(senderPublic.size.toByte())
            .put(senderPublic)
            .put(ciphertext)
            .array()
    }

    fun vapidAuthorization(
        endpoint: String,
        subject: String,
        keys: KeyPair,
        now: Instant = Instant.now(),
        validity: Duration = Duration.ofHours(12),
    ): String {
        val uri = URI.create(endpoint)
        val audience = "${uri.scheme}://${uri.rawAuthority}"
        val header = """{"typ":"JWT","alg":"ES256"}"""
        val claims =
            """{"aud":"${jsonEscape(audience)}","exp":${now.plus(validity).epochSecond},"sub":"${jsonEscape(subject)}"}"""

        val signingInput = base64Url(header.toByteArray()) + "." + base64Url(claims.toByteArray())
        val signature =
            Signature.getInstance("SHA256withECDSAinP1363Format").run {
                initSign(keys.private)
                update(signingInput.toByteArray())
                sign()
            }

        return "vapid t=$signingInput.${base64Url(signature)}, k=${base64Url(rawPublicKey(keys.public as ECPublicKey))}"
    }

    private fun curveParameters(): ECParameterSpec =
        AlgorithmParameters
            .getInstance("EC")
            .apply { init(ECGenParameterSpec(CURVE)) }
            .getParameterSpec(ECParameterSpec::class.java)

    private fun jsonEscape(value: String) = value.replace("\\", "\\\\").replace("\"", "\\\"")

    private fun hkdf(
        salt: ByteArray,
        ikm: ByteArray,
        info: ByteArray,
        length: Int,
    ) = expand(hmac(salt, ikm), info, length)

    private fun expand(
        prk: ByteArray,
        info: ByteArray,
        length: Int,
    ) = hmac(prk, info + FIRST_BLOCK).copyOf(length)

    private fun hmac(
        key: ByteArray,
        data: ByteArray,
    ): ByteArray =
        Mac.getInstance("HmacSHA256").run {
            init(SecretKeySpec(key, "HmacSHA256"))
            doFinal(data)
        }

    private fun unsigned(value: BigInteger): ByteArray {
        val bytes = value.toByteArray()
        return when {
            bytes.size == COORDINATE_LENGTH -> bytes
            bytes.size > COORDINATE_LENGTH -> bytes.copyOfRange(bytes.size - COORDINATE_LENGTH, bytes.size)
            else -> ByteArray(COORDINATE_LENGTH - bytes.size) + bytes
        }
    }
}
