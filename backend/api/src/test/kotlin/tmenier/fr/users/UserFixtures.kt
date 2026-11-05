package tmenier.fr.users

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.auth.services.JwtService
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.databases.mappers.UserDto
import tmenier.fr.databases.repositories.UserRepository
import java.util.UUID

@ApplicationScoped
class UserFixtures(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
) {
    @Transactional
    fun user(role: UserRole = UserRole.ADMIN): UserDto {
        val suffix = UUID.randomUUID().toString().take(8)
        val user =
            UserDto(
                id = UUID.randomUUID(),
                name = "User $suffix",
                email = "user-$suffix@example.com",
                password = "not-a-hash",
                role = role,
            )

        userRepository.store(user)

        return user
    }

    fun token(user: UserDto): String = jwtService.generateJwt(user.id, user.name, user.email, user.role)

    fun token(role: UserRole = UserRole.ADMIN): String = token(user(role))

    @Transactional
    fun delete(id: UUID) {
        userRepository.delete(id)
    }
}
