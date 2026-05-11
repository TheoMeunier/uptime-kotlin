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
)

object RefreshTokenMapper {

    fun fromEntity(refreshTokenEntity: RefreshTokenEntity): RefreshTokenDto {
        return RefreshTokenDto(
            id = refreshTokenEntity.id,
            token = refreshTokenEntity.refreshToken,
            expiredAt = refreshTokenEntity.expiredAt,
            user = refreshTokenEntity.user.let { UserMapper.fromEntity(it) },
        )
    }

    fun toEntity(refreshTokenDto: RefreshTokenDto): RefreshTokenEntity {
        return RefreshTokenEntity().apply {
            id = refreshTokenDto.id
            refreshToken = refreshTokenDto.token
            expiredAt = refreshTokenDto.expiredAt
            user = refreshTokenDto.user?.let { UserMapper.toEntity(it) } ?: throw IllegalArgumentException("User cannot be null")
        }
    }

}

