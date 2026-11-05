package tmenier.fr.auth.services

import io.quarkus.security.AuthenticationFailedException
import io.quarkus.security.identity.AuthenticationRequestContext
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.identity.SecurityIdentityAugmentor
import io.quarkus.security.runtime.QuarkusSecurityIdentity
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class UserRoleAugmentor(
    private val userRoleLookup: UserRoleLookup,
) : SecurityIdentityAugmentor {
    override fun augment(
        identity: SecurityIdentity,
        context: AuthenticationRequestContext,
    ): Uni<SecurityIdentity> {
        if (identity.isAnonymous) return Uni.createFrom().item(identity)

        return context.runBlocking {
            val userId =
                runCatching { UUID.fromString(identity.principal.name) }
                    .getOrElse { throw AuthenticationFailedException("Unknown account") }

            val role = userRoleLookup.roleOf(userId) ?: throw AuthenticationFailedException("Unknown account")

            QuarkusSecurityIdentity
                .builder(identity)
                .addRole(role.securityRole)
                .build()
        }
    }
}
