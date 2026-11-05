package tmenier.fr.users.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.databases.dtos.UserListItemDto
import tmenier.fr.databases.mappers.UserMapper
import tmenier.fr.databases.repositories.UserRepository
import java.util.UUID

@ApplicationScoped
class ListUsersAction(
    private val userRepository: UserRepository,
) {
    fun execute(currentUserId: UUID): List<UserListItemDto> = userRepository.findAllOrdered().map { UserMapper.toListItemDto(it, currentUserId) }
}
