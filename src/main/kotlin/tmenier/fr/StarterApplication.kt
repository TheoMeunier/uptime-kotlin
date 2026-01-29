package tmenier.fr

import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import tmenier.fr.auth.seeder.CreateDefaultUserSeed
import tmenier.fr.common.utils.logger

@Singleton
class StarterApplication {

    @Inject
    lateinit var createDefaultUserSeed: CreateDefaultUserSeed

    @Transactional
    fun onStart(@Observes event: StartupEvent) {
        logger.info { "Application Starting" }
        execute()
    }

    private fun execute() {
        createDefaultUserSeed.execute()
    }
}