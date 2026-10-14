package tmenier.fr.common.config

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class DemoMode(
    @ConfigProperty(name = "demo.enabled", defaultValue = "false") val enabled: Boolean,
    @ConfigProperty(name = "demo.email", defaultValue = "demo@uptime-kotlin.dev") val email: String,
    @ConfigProperty(name = "demo.password", defaultValue = "demo-uptime") val password: String,
)
