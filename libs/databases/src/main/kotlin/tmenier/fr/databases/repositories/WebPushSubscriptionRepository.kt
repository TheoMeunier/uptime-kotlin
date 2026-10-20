package tmenier.fr.databases.repositories

import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum

@ApplicationScoped
class WebPushSubscriptionRepository(
    private val em: EntityManager,
) {
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun removeEndpoint(endpoint: String): Int =
        em
            .createNativeQuery(
                """
                UPDATE notifications_channels
                SET content = jsonb_set(
                        content,
                        '{subscriptions}',
                        COALESCE((SELECT jsonb_agg(s)
                                  FROM jsonb_array_elements(content -> 'subscriptions') AS s
                                  WHERE s ->> 'endpoint' <> CAST(:endpoint AS text)), CAST('[]' AS jsonb))),
                    updated_at = NOW()
                WHERE type = :type
                  AND content -> 'subscriptions' @> jsonb_build_array(jsonb_build_object('endpoint', CAST(:endpoint AS text)))
                """.trimIndent(),
            ).setParameter("endpoint", endpoint)
            .setParameter("type", NotificationChannelsEnum.WEB_PUSH.ordinal)
            .executeUpdate()
}
