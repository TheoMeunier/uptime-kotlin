package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.NotFoundException
import tmenier.fr.databases.entities.UserEntity
import tmenier.fr.databases.mappers.UserDto
import tmenier.fr.databases.mappers.UserMapper
import java.util.UUID

@ApplicationScoped
class UserRepository : PanacheRepository<UserEntity> {
    fun findById(id: UUID): UserEntity = find("id = ?1", id).firstResult() ?: throw NotFoundException("User not found with id")

    fun findByEmail(email: String): UserDto {
        val user = find("email = ?1", email).firstResult() ?: throw NotFoundException("User not found with email: $email")

        return UserMapper.fromEntity(user)
    }

    fun countAll() = count()

    fun store(dto: UserDto) {
        UserEntity()
            .apply {
                id = UUID.randomUUID()
                name = dto.name
                email = dto.email
                password = dto.password
            }.persist()
    }

    fun update(userDto: UserDto) {
        val user = find("id = ?1", userDto.id).firstResult() ?: throw NotFoundException("User not found with id: ${userDto.id}")

        user.name = userDto.name
        user.email = userDto.email

        user.persist()
    }

    fun updatePassword(
        userId: UUID,
        password: String,
    ) {
        val userEntity = find("id = ?1", userId).firstResult() ?: throw NotFoundException("User not found with id")

        userEntity.password = password
        userEntity.persist()
    }
}
