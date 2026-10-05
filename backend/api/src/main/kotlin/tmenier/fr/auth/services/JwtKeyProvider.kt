package tmenier.fr.auth.services

import io.quarkus.runtime.Startup
import io.smallrye.jwt.util.KeyUtils
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.JwtSigningKeyRepository
import tmenier.fr.databases.repositories.StoredJwtSigningKey
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Base64
import java.util.Optional

@Startup
@ApplicationScoped
class JwtKeyProvider(
    repository: JwtSigningKeyRepository,
    encryptionService: EncryptionService,
    @ConfigProperty(name = "mp.jwt.verify.publickey.location") publicKeyLocation: Optional<String>,
    @ConfigProperty(name = "smallrye.jwt.sign.key.location") privateKeyLocation: Optional<String>,
) {
    private val keys: KeyPair =
        load(
            repository = repository,
            encryptionService = encryptionService,
            publicLocation = publicKeyLocation.orElse(null)?.takeIf { it.isNotBlank() },
            privateLocation = privateKeyLocation.orElse(null)?.takeIf { it.isNotBlank() },
        )

    val publicKey: PublicKey
        get() = keys.public

    val privateKey: PrivateKey
        get() = keys.private

    private companion object {
        const val KEY_SIZE = 2048

        fun load(
            repository: JwtSigningKeyRepository,
            encryptionService: EncryptionService,
            publicLocation: String?,
            privateLocation: String?,
        ): KeyPair {
            check((publicLocation == null) == (privateLocation == null)) {
                "MP_JWT_VERIFY_PUBLICKEY_LOCATION and SMALLRYE_JWT_SIGN_KEY_LOCATION must be set together, " +
                    "or both left unset so that the API generates and stores its own key pair."
            }

            if (publicLocation != null && privateLocation != null) {
                logger.info { "JWT keys loaded from $publicLocation and $privateLocation" }
                return KeyPair(KeyUtils.readPublicKey(publicLocation), KeyUtils.readPrivateKey(privateLocation))
            }

            val stored = repository.findOrCreate { generate(encryptionService) }
            val privateKeyContent =
                runCatching { encryptionService.decrypt(stored.privateKey) }.getOrElse {
                    throw IllegalStateException(
                        "Unable to decrypt the JWT signing key stored in jwt_signing_keys: " +
                            "ENCRYPTION_MASTER_KEY is not the key it was encrypted with.",
                        it,
                    )
                }

            logger.info { "JWT keys loaded from the database" }
            return KeyPair(KeyUtils.decodePublicKey(stored.publicKey), KeyUtils.decodePrivateKey(privateKeyContent))
        }

        fun generate(encryptionService: EncryptionService): StoredJwtSigningKey {
            val pair = KeyUtils.generateKeyPair(KEY_SIZE)
            val encoder = Base64.getEncoder()

            return StoredJwtSigningKey(
                publicKey = encoder.encodeToString(pair.public.encoded),
                privateKey = encryptionService.encrypt(encoder.encodeToString(pair.private.encoded)),
            )
        }
    }
}
