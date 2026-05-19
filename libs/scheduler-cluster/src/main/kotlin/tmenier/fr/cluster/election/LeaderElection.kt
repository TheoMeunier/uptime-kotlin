package tmenier.fr.cluster.election

import io.quarkus.redis.datasource.ReactiveRedisDataSource
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.utils.logger
import java.util.UUID

@ApplicationScoped
class LeaderElection(
    private val redis: ReactiveRedisDataSource,
) {
    private val instanceId = UUID.randomUUID().toString()
    private val leaderKey = "probe:scheduler:leader"
    private val ttlSeconds = 10L // expired after 10s

    fun isLeader(): Boolean =
        try {
            val result =
                redis
                    .value(String::class.java)
                    .setnx(leaderKey, instanceId)
                    .await()
                    .indefinitely()

            logger.info { "setnx result: $result, instanceId: $instanceId" }

            if (result) {
                redis
                    .key()
                    .expire(leaderKey, ttlSeconds)
                    .await()
                    .indefinitely()
                logger.info { "Became leader: $instanceId" }
                true
            } else {
                val current =
                    redis
                        .value(String::class.java)
                        .get(leaderKey)
                        .await()
                        .indefinitely()

                logger.info { "Current leader in Redis: $current, mine: $instanceId" }

                if (current == instanceId) {
                    redis
                        .key()
                        .expire(leaderKey, ttlSeconds)
                        .await()
                        .indefinitely()
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Leader election failed: ${e.message}" }
            false
        }
}
