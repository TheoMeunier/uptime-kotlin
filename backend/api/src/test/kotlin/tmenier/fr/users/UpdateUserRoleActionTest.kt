package tmenier.fr.users

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.common.exceptions.core.ApiException
import tmenier.fr.databases.entities.UserEntity
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.users.actions.RemoveUserAction
import tmenier.fr.users.actions.UpdateUserRoleAction
import java.util.UUID

class UpdateUserRoleActionTest {
    @Test
    fun `the last admin can be neither demoted nor removed`() {
        val admin = user(UserRole.ADMIN)
        val repository = FakeUserRepository(admin, user(UserRole.VIEWER))

        val demote = assertThrows<ApiException> { UpdateUserRoleAction(repository).execute(UUID.randomUUID(), admin.id, UserRole.VIEWER) }
        val remove = assertThrows<ApiException> { RemoveUserAction(repository).execute(UUID.randomUUID(), admin.id) }

        assertEquals(UpdateUserRoleAction.LAST_ADMIN, demote.errorCode)
        assertEquals(UpdateUserRoleAction.LAST_ADMIN, remove.errorCode)
    }

    @Test
    fun `an admin can be demoted while another admin remains`() {
        val current = user(UserRole.ADMIN)
        val other = user(UserRole.ADMIN)
        val repository = FakeUserRepository(current, other)

        UpdateUserRoleAction(repository).execute(current.id, other.id, UserRole.VIEWER)

        assertEquals(UserRole.VIEWER, other.role)
    }

    @Test
    fun `nobody changes their own role or removes themselves`() {
        val admin = user(UserRole.ADMIN)
        val repository = FakeUserRepository(admin, user(UserRole.ADMIN))

        val update = assertThrows<ApiException> { UpdateUserRoleAction(repository).execute(admin.id, admin.id, UserRole.VIEWER) }
        val remove = assertThrows<ApiException> { RemoveUserAction(repository).execute(admin.id, admin.id) }

        assertEquals(UpdateUserRoleAction.OWN_ACCOUNT, update.errorCode)
        assertEquals(UpdateUserRoleAction.OWN_ACCOUNT, remove.errorCode)
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

    override fun updateRole(
        id: UUID,
        role: UserRole,
    ) {
        users.getValue(id).role = role
    }

    override fun delete(id: UUID): Boolean = users.remove(id) != null
}
