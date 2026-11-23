package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.LockModeType
import jakarta.ws.rs.NotFoundException
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.databases.entities.UserEntity
import tmenier.fr.databases.mappers.UserDto
import tmenier.fr.databases.mappers.UserMapper
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class UserRepository : PanacheRepository<UserEntity> {
    fun findById(id: UUID): UserEntity = find("id = ?1", id).firstResult() ?: throw NotFoundException("User not found with id")

    fun findByEmailOrNull(email: String): UserDto? =
        find("email = ?1", email)
            .firstResult()
            ?.let { UserMapper.fromEntity(it) }

    fun findByIdOrNull(id: UUID): UserEntity? = find("id = ?1", id).firstResult()

    fun existsByEmail(
        email: String,
        excludingId: UUID? = null,
    ): Boolean =
        if (excludingId == null) {
            count("lower(email) = lower(?1)", email) > 0
        } else {
            count("lower(email) = lower(?1) and id <> ?2", email, excludingId) > 0
        }

    fun findAllOrdered(): List<UserEntity> = list("order by createdAt asc, name asc")

    fun roleOf(id: UUID): UserRole? = findByIdOrNull(id)?.role

    fun findByEmailIgnoreCaseOrNull(email: String): UserEntity? = find("lower(email) = lower(?1)", email.trim()).firstResult()

    fun lockAdmins(): List<UserEntity> =
        find("role = ?1 order by id", UserRole.ADMIN)
            .withLock(LockModeType.PESSIMISTIC_WRITE)
            .list()

    fun delete(id: UUID): Boolean = delete("id = ?1", id) > 0

    fun countAll() = count()

    fun store(dto: UserDto) {
        UserEntity()
            .apply {
                id = dto.id
                name = dto.name
                email = dto.email
                password = dto.password
                role = dto.role
            }.persist()
    }

    fun update(userDto: UserDto) {
        val user = find("id = ?1", userDto.id).firstResult() ?: throw NotFoundException("User not found with id: ${userDto.id}")

        user.name = userDto.name
        user.email = userDto.email

        user.persist()
    }

    fun updatePassword(
        userId: UUID,
        password: String,
    ) {
        val userEntity = find("id = ?1", userId).firstResult() ?: throw NotFoundException("User not found with id")

        userEntity.password = password
        userEntity.mustChangePassword = false
        userEntity.persist()
    }

    fun setTemporaryPassword(
        userEntity: UserEntity,
        passwordHash: String,
    ) {
        userEntity.password = passwordHash
        userEntity.mustChangePassword = true
        userEntity.persist()
    }

    fun startTwoFactorSetup(
        userEntity: UserEntity,
        encryptedSecret: String,
    ) {
        userEntity.totpSecret = encryptedSecret
        userEntity.totpEnabledAt = null
        userEntity.totpLastStep = null
        userEntity.persist()
    }

    fun enableTwoFactor(
        userEntity: UserEntity,
        acceptedStep: Long,
        at: Instant = Instant.now(),
    ) {
        userEntity.totpEnabledAt = at
        userEntity.totpLastStep = acceptedStep
        userEntity.persist()
    }

    fun recordTotpStep(
        userEntity: UserEntity,
        step: Long,
    ) {
        userEntity.totpLastStep = step
        userEntity.persist()
    }

    fun disableTwoFactor(userEntity: UserEntity) {
        userEntity.totpSecret = null
        userEntity.totpEnabledAt = null
        userEntity.totpLastStep = null
        userEntity.persist()
    }

    fun findTotpSecrets(): List<Pair<UUID, String>> = list("totpSecret is not null").map { it.id to it.totpSecret!! }

    fun replaceTotpSecret(
        id: UUID,
        previous: String,
        next: String,
    ): Boolean = update("totpSecret = ?1 where id = ?2 and totpSecret = ?3", next, id, previous) > 0

    fun isOnboardingCompleted(userId: UUID): Boolean = findById(userId).onboardingCompletedAt != null

    fun markOnboardingCompleted(
        userId: UUID,
        at: Instant,
    ) {
        val userEntity = findById(userId)

        if (userEntity.onboardingCompletedAt == null) {
            userEntity.onboardingCompletedAt = at
            userEntity.persist()
        }
    }
}
