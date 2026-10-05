package tmenier.fr.auth.services

import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipalFactory
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory
import jakarta.annotation.Priority
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Alternative

@ApplicationScoped
@Alternative
@Priority(1)
class ManagedKeyJwtCallerPrincipalFactory(
    private val keyProvider: JwtKeyProvider,
) : JWTCallerPrincipalFactory() {
    private val delegate = DefaultJWTCallerPrincipalFactory()

    override fun parse(
        token: String,
        authContextInfo: JWTAuthContextInfo,
    ): JWTCallerPrincipal {
        val contextInfo = JWTAuthContextInfo(authContextInfo)
        contextInfo.publicVerificationKey = keyProvider.publicKey

        return delegate.parse(token, contextInfo)
    }
}
