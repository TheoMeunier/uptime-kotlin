package tmenier.fr.databases.mappers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.dtos.NotificationDto
import java.time.Instant
import java.util.UUID

class NotificationMapperTest {
    private val gotify =
        NotificationContent.Gotify(
            serverUrl = "https://gotify.example.com",
            token = "enc:v2:encrypted-token",
            priority = 8,
        )

    private fun gotifyEntity() =
        NotificationMapper
            .toEntity(
                NotificationDto(
                    id = UUID.randomUUID(),
                    name = "Gotify",
                    type = NotificationChannelsEnum.GOTIFY,
                    isDefault = false,
                    content = gotify,
                ),
            ).apply { createdAt = Instant.EPOCH }

    @Test
    fun `gotify content round-trips through the entity with its encrypted token`() {
        val entity = gotifyEntity()

        assertEquals(NotificationChannelsEnum.GOTIFY, NotificationContentMapper.toEntity(gotify).second)
        assertEquals(gotify, NotificationMapper.toDto(entity).content)
    }

    @Test
    fun `the token is never sent back to the browser`() {
        val shown = NotificationMapper.toShowDto(gotifyEntity()).content as NotificationContent.Gotify

        assertNull(shown.token)
        assertEquals("https://gotify.example.com", shown.serverUrl)
        assertEquals(8, shown.priority)
    }

    @Test
    fun `gotify is appended after the existing channels because the type is stored by ordinal`() {
        assertEquals(5, NotificationChannelsEnum.GOTIFY.ordinal)
    }
}
