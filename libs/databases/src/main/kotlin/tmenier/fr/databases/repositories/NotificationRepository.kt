package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.dtos.NotificationDto
import tmenier.fr.databases.entities.NotificationsChannelEntity
import tmenier.fr.databases.mappers.NotificationContentMapper
import tmenier.fr.databases.mappers.NotificationMapper
import java.util.UUID

@ApplicationScoped
class NotificationRepository : PanacheRepositoryBase<NotificationsChannelEntity, UUID> {
    override fun findById(id: UUID): NotificationsChannelEntity =
        find("id = ?1", id).firstResult() ?: throw NotFoundException("Notification channel not found: $id")

    fun findByIds(ids: List<UUID>): List<NotificationsChannelEntity> = find("id in ?1 OR isDefault", ids).list()

    fun getAll(): List<NotificationsChannelEntity> = findAll().list()

    fun save(dto: NotificationDto) {
        val entity = NotificationMapper.toEntity(dto)
        entity.persist()
    }

    fun update(dto: NotificationDto) {
        val entity = findById(dto.id)
        entity.name = dto.name
        entity.type = dto.type
        entity.isDefault = dto.isDefault
        entity.content = NotificationContentMapper.toEntity(dto.content).first
        entity.persist()
    }

    fun delete(id: UUID) = delete("id = ?1", id)
}
