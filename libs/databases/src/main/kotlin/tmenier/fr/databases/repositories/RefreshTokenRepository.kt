package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.exceptions.common.InvalidCredentialsException
import tmenier.fr.databases.entities.RefreshTokenEntity
import tmenier.fr.databases.mappers.RefreshTokenDto
import tmenier.fr.databases.mappers.RefreshTokenMapper
import tmenier.fr.databases.mappers.UserDto
import tmenier.fr.databases.mappers.UserMapper
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class RefreshTokenRepository : PanacheRepositoryBase<RefreshTokenEntity, UUID> {
    fun findByRefreshToken(refreshToken: UUID): RefreshTokenDto {
        val rt = find("refreshToken = ?1", refreshToken).firstResult() ?: throw InvalidCredentialsException()

        return RefreshTokenMapper.fromEntity(rt)
    }

    fun findActiveByUser(userId: UUID): List<RefreshTokenDto> =
        find(
            "user.id = ?1 and expiredAt > ?2",
            Sort.by("createdAt", Sort.Direction.Descending),
            userId,
            LocalDateTime.now(),
        ).list().map(RefreshTokenMapper::fromEntity)

    fun storeRefreshToken(
        refreshToken: UUID,
        userDto: UserDto,
        userAgent: String? = null,
        ipAddress: String? = null,
    ): UUID {
        val id = UUID.randomUUID()

        RefreshTokenEntity()
            .apply {
                this.id = id
                this.user = UserMapper.toEntity(userDto)
                this.refreshToken = refreshToken
                createdAt = LocalDateTime.now()
                expiredAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_LIFETIME_DAYS)
                this.userAgent = userAgent?.take(USER_AGENT_MAX_LENGTH)
                this.ipAddress = ipAddress?.take(IP_ADDRESS_MAX_LENGTH)
            }.persist()

        return id
    }

    fun rotateRefreshToken(
        id: UUID,
        newRefreshToken: UUID,
        userAgent: String? = null,
        ipAddress: String? = null,
    ): Boolean {
        val now = LocalDateTime.now()

        return update(
            "refreshToken = ?1, lastUsedAt = ?2, expiredAt = ?3, userAgent = ?4, ipAddress = ?5 where id = ?6",
            newRefreshToken,
            now,
            now.plusDays(REFRESH_TOKEN_LIFETIME_DAYS),
            userAgent?.take(USER_AGENT_MAX_LENGTH),
            ipAddress?.take(IP_ADDRESS_MAX_LENGTH),
            id,
        ) > 0
    }

    fun revokeByRefreshToken(refreshToken: UUID): Boolean = delete("refreshToken = ?1", refreshToken) > 0

    fun revokeByIdForUser(
        id: UUID,
        userId: UUID,
    ): Boolean = delete("id = ?1 and user.id = ?2", id, userId) > 0

    fun revokeAllForUser(userId: UUID): Long = delete("user.id = ?1", userId)

    fun purgeExpired(now: LocalDateTime = LocalDateTime.now()): Long = delete("expiredAt <= ?1", now)

    companion object {
        const val REFRESH_TOKEN_LIFETIME_DAYS = 3L
        private const val USER_AGENT_MAX_LENGTH = 512
        private const val IP_ADDRESS_MAX_LENGTH = 45
    }
}
