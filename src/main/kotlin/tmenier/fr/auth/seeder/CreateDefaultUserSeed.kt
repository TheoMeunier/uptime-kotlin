package tmenier.fr.auth.seeder

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import tmenier.fr.auth.entities.UserEntity
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.common.utils.logger
import java.util.*

@ApplicationScoped
class CreateDefaultUserSeed {

    @Inject
    lateinit var bcryptService: BcryptService

    @Transactional
    fun execute() {
        val defaultUser = UserEntity.findByEmail("admin@uptime-kotlin.com")

        if (defaultUser === null) {
            val user = UserEntity().apply {
                id = UUID.randomUUID()
                name = "admin"
                email = "admin@uptime-kotlin.com"
                password = bcryptService.hashPassword("adminadmin")
            }

            user.persist()

            return logger.info { "Default user create successfully" }
        }

        return logger.info { "Default user already exists" }
    }
}