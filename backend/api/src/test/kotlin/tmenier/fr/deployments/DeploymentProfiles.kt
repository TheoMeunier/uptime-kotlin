package tmenier.fr.deployments

import io.quarkus.test.junit.QuarkusTestProfile

abstract class DeploymentProfile(
    private val overrides: Map<String, String>,
) : QuarkusTestProfile {
    override fun getConfigOverrides(): Map<String, String> =
        mapOf(
            "quarkus.datasource.jdbc.url" to
                "\${UPTIME_TEST_JDBC_URL:jdbc:postgresql://localhost:5432/uptime-kotlin-test}",
            "quarkus.devservices.enabled" to "false",
            "scheduler.worker.concurrency" to "2",
        ) + overrides
}

class SingleInstanceProfile :
    DeploymentProfile(
        mapOf(
            "scheduler.strategy" to "database",
            "scheduler.worker.name" to REGION,
        ),
    ) {
    companion object {
        const val REGION = "it-single-instance"
    }
}

class ApiOnlyProfile :
    DeploymentProfile(
        mapOf(
            "scheduler.strategy" to "none",
            "scheduler.worker.name" to REGION,
        ),
    ) {
    companion object {
        const val REGION = "it-api-only"
    }
}

class LegacyDbLockProfile :
    DeploymentProfile(
        mapOf(
            "scheduler.strategy" to "",
            "quarkus.scheduler.strategy" to "db-lock",
            "scheduler.worker.name" to REGION,
        ),
    ) {
    companion object {
        const val REGION = "it-legacy-db-lock"
    }
}

class MetricsProfile :
    DeploymentProfile(
        mapOf(
            "scheduler.strategy" to "database",
            "scheduler.worker.name" to REGION,
            "quarkus.management.enabled" to "true",
        ),
    ) {
    companion object {
        const val REGION = "it-metrics"
    }
}

abstract class ReverseProxyProfile(
    trustedProxies: String,
) : DeploymentProfile(
        mapOf(
            "scheduler.strategy" to "none",
            "scheduler.worker.name" to "it-reverse-proxy",
            "quarkus.http.proxy.proxy-address-forwarding" to "true",
            "quarkus.http.proxy.trusted-proxies" to trustedProxies,
        ),
    )

class TrustedProxyProfile : ReverseProxyProfile("127.0.0.0/8,::1/128")

class UntrustedProxyProfile : ReverseProxyProfile("203.0.113.10")
