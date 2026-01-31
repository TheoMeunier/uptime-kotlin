package tmenier.fr.setup.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.auth.entities.UserEntity

@ApplicationScoped
class GetStatusApplicationAction {
    fun execute(): Boolean {
        val totalUser = UserEntity.count()

        return totalUser.toInt() > 0
    }
}