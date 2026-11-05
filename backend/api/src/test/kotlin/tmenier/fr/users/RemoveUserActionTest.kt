package tmenier.fr.users

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.common.exceptions.core.ApiException
import tmenier.fr.databases.entities.UserEntity
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.users.actions.RemoveUserAction
import java.util.UUID

class RemoveUserActionTest {
    @Test
    fun `the last admin cannot be removed`() {
        val admin = user(UserRole.ADMIN)
        val repository = FakeUserRepository(admin, user(UserRole.VIEWER))

        val exception = assertThrows<ApiException> { RemoveUserAction(repository).execute(UUID.randomUUID(), admin.id) }

        assertEquals(RemoveUserAction.LAST_ADMIN, exception.errorCode)
    }

    @Test
    fun `an admin can be removed while another admin remains`() {
        val current = user(UserRole.ADMIN)
        val other = user(UserRole.ADMIN)
        val repository = FakeUserRepository(current, other)

        RemoveUserAction(repository).execute(current.id, other.id)

        assertTrue(repository.findByIdOrNull(other.id) == null)
    }

    @Test
    fun `nobody removes themselves`() {
        val admin = user(UserRole.ADMIN)
        val repository = FakeUserRepository(admin, user(UserRole.ADMIN))

        val exception = assertThrows<ApiException> { RemoveUserAction(repository).execute(admin.id, admin.id) }

        assertEquals(RemoveUserAction.OWN_ACCOUNT, exception.errorCode)
    }

    private fun user(role: UserRole) =
        UserEntity().apply {
            id = UUID.randomUUID()
            name = "User"
            email = "${id.toString().take(8)}@example.com"
            password = "hash"
            this.role = role
        }
}

private class FakeUserRepository(
    vararg entities: UserEntity,
) : UserRepository() {
    private val users = entities.associateBy { it.id }.toMutableMap()

    override fun lockAdmins(): List<UserEntity> = users.values.filter { it.role == UserRole.ADMIN }

    override fun findByIdOrNull(id: UUID): UserEntity? = users[id]

    override fun delete(id: UUID): Boolean = users.remove(id) != null
}
