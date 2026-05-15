package tmenier.fr.setup.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.databases.repositories.UserRepository

@ApplicationScoped
class GetStatusApplicationAction(
    private val userRepository: UserRepository,
) {
    fun execute(): Boolean {
        val totalUser = userRepository.countAll()

        return totalUser.toInt() > 0
    }
}
