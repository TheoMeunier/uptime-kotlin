package tmenier.fr.databases.repositories

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.NotFoundException
import tmenier.fr.databases.entities.UserEntity
import tmenier.fr.databases.mappers.UserDto
import tmenier.fr.databases.mappers.UserMapper
import java.util.UUID

@ApplicationScoped
class UserRepository {

    fun findById(id: UUID): UserDto {
        val user = UserEntity.findById(id) ?: throw NotFoundException("User not found with id")
        return UserMapper.fromEntity(user)
    }

    fun findByEmail(email: String): UserDto {
        val user = UserEntity.findByEmail(email) ?: throw NotFoundException("User not found with email: $email")

        return UserMapper.fromEntity(user)
    }

    fun count() = UserEntity.count()

    fun store(dto: UserDto) {
        UserEntity().apply {
            id = UUID.randomUUID()
            name = dto.name
            email = dto.email
            password = dto.password
        }.persist()
    }

    fun update(userDto: UserDto) {
        val user = UserEntity.findById(userDto.id) ?: throw NotFoundException("User not found with id: ${userDto.id}")

        user.name = userDto.name
        user.email = userDto.email

        user.persist()
    }

    fun updatePassword(userId: UUID, password: String) {
        val userEntity = UserEntity.findById(userId) ?: throw NotFoundException("User not found with id")

        userEntity.password = password
        userEntity.persist()
    }
}

