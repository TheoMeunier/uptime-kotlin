package tmenier.fr.databases.repositories

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import java.util.UUID

enum class EncryptedContentTable(
    val tableName: String,
) {
    PROBES("probes"),
    NOTIFICATION_CHANNELS("notifications_channels"),
}

@RegisterForReflection
data class EncryptedContentRow(
    val id: UUID,
    val content: String,
)

@ApplicationScoped
class EncryptedContentRepository(
    private val em: EntityManager,
) {
    fun findWithEncryptedValues(table: EncryptedContentTable): List<EncryptedContentRow> =
        em
            .createNativeQuery(
                "SELECT id, CAST(content AS text) FROM ${table.tableName} " +
                    "WHERE CAST(content AS text) LIKE :marker ORDER BY id",
            ).setParameter("marker", "%\"enc:v_:%")
            .resultList
            .map { row ->
                row as Array<*>
                EncryptedContentRow(id = row[0] as UUID, content = row[1] as String)
            }

    fun replaceContent(
        table: EncryptedContentTable,
        id: UUID,
        previous: String,
        next: String,
    ): Boolean =
        em
            .createNativeQuery(
                "UPDATE ${table.tableName} SET content = CAST(:next AS jsonb) " +
                    "WHERE id = :id AND content = CAST(:previous AS jsonb)",
            ).setParameter("next", next)
            .setParameter("id", id)
            .setParameter("previous", previous)
            .executeUpdate() == 1
}
