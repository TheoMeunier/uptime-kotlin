package tmenier.fr.common.config

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty

/**
 * Public demo instance (`DEMO_ENABLED=true`).
 *
 * The API refuses every write except login, refresh, logout and "check now", seeds a demo account on an empty
 * database, and nothing is delivered to notification channels. Set it on the API **and** on the workers.
 */
@ApplicationScoped
class DemoMode(
    @ConfigProperty(name = "demo.enabled", defaultValue = "false") val enabled: Boolean,
    @ConfigProperty(name = "demo.email", defaultValue = "demo@uptime-kotlin.dev") val email: String,
    @ConfigProperty(name = "demo.password", defaultValue = "demo-uptime") val password: String,
)
