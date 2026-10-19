package tmenier.fr.common.validations

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.net.Inet6Address
import java.net.InetAddress
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [HostnameOrIpValidator::class])
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class HostnameOrIp(
    val message: String = "Must be a valid hostname or IP address",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class HostnameOrIpValidator : ConstraintValidator<HostnameOrIp, String> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext,
    ): Boolean = isValidHostnameOrIp(value)

    companion object {
        private val LEGACY_SCHEME = Regex("^https?://", RegexOption.IGNORE_CASE)
        private val LABEL = Regex("^[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?$")
        private val IPV4 = Regex("^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$")

        fun isValidHostnameOrIp(value: String?): Boolean {
            if (value.isNullOrBlank()) return false
            val host = value.trim().replaceFirst(LEGACY_SCHEME, "")
            if (host.isEmpty()) return false

            if (host.contains(':')) return isValidIpv6(host.removePrefix("[").removeSuffix("]"))

            return isValidHostname(host)
        }

        private fun isValidHostname(host: String): Boolean {
            val name = host.removeSuffix(".")
            if (name.isEmpty() || name.length > 253) return false

            val labels = name.split('.')
            if (!labels.all { LABEL.matches(it) }) return false

            return if (labels.last().all { it.isDigit() }) IPV4.matches(name) else true
        }

        private fun isValidIpv6(host: String): Boolean =
            try {
                InetAddress.getByName(host) is Inet6Address
            } catch (e: Exception) {
                false
            }
    }
}
