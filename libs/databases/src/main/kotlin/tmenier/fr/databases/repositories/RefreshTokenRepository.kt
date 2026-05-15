package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
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

    fun storeRefreshToken(refreshToken: UUID, userDto: UserDto) {
        RefreshTokenEntity()
            .apply {
                id = UUID.randomUUID()
                this.user = UserMapper.toEntity(userDto)
                this.refreshToken = refreshToken
                expiredAt = LocalDateTime.now().plusDays(3)
            }.persist()
    }

    fun delete(rt: RefreshTokenDto) = RefreshTokenMapper.toEntity(rt).delete()
}


