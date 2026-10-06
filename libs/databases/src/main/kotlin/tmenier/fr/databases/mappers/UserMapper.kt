package tmenier.fr.databases.mappers

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.databases.entities.UserEntity
import java.time.Instant
import java.util.UUID

@RegisterForReflection
data class UserDto(
    val id: UUID,
    val name: String,
    val email: String,
    val password: String,
    val createdAt: Instant? = null,
)

object UserMapper {
    fun fromEntity(userEntity: UserEntity): UserDto =
        UserDto(
            id = userEntity.id,
            name = userEntity.name,
            email = userEntity.email,
            password = userEntity.password,
            createdAt = userEntity.createdAt,
        )

    fun toEntity(userDto: UserDto): UserEntity =
        UserEntity().apply {
            id = userDto.id
            name = userDto.name
            email = userDto.email
            password = userDto.password
            createdAt = userDto.createdAt ?: Instant.now()
        }
}
