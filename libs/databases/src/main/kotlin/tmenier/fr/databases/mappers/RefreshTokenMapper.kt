package tmenier.fr.databases.mappers

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.databases.entities.RefreshTokenEntity
import java.time.LocalDateTime
import java.util.UUID

@RegisterForReflection
data class RefreshTokenDto(
    val id: UUID,
    val token: UUID,
    val expiredAt: LocalDateTime,
    val user: UserDto,
    val createdAt: LocalDateTime,
    val lastUsedAt: LocalDateTime? = null,
    val userAgent: String? = null,
    val ipAddress: String? = null,
)

object RefreshTokenMapper {
    fun fromEntity(refreshTokenEntity: RefreshTokenEntity): RefreshTokenDto =
        RefreshTokenDto(
            id = refreshTokenEntity.id,
            token = refreshTokenEntity.refreshToken,
            expiredAt = refreshTokenEntity.expiredAt,
            user = refreshTokenEntity.user.let { UserMapper.fromEntity(it) },
            createdAt = refreshTokenEntity.createdAt,
            lastUsedAt = refreshTokenEntity.lastUsedAt,
            userAgent = refreshTokenEntity.userAgent,
            ipAddress = refreshTokenEntity.ipAddress,
        )

    fun toEntity(refreshTokenDto: RefreshTokenDto): RefreshTokenEntity =
        RefreshTokenEntity().apply {
            id = refreshTokenDto.id
            refreshToken = refreshTokenDto.token
            expiredAt = refreshTokenDto.expiredAt
            user = refreshTokenDto.user.let { UserMapper.toEntity(it) }
            createdAt = refreshTokenDto.createdAt
            lastUsedAt = refreshTokenDto.lastUsedAt
            userAgent = refreshTokenDto.userAgent
            ipAddress = refreshTokenDto.ipAddress
        }
}
