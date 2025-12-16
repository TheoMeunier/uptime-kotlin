package tmenier.fr.common.validations

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.net.Inet4Address
import java.net.InetAddress
import java.net.URI
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [UrlOrIpValidator::class])
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class UrlOrIp(
    val message: String = "Must be a valid https URL or IPv4 address",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class UrlOrIpValidator : ConstraintValidator<UrlOrIp, String> {

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value.isNullOrBlank()) return false

        return isValidHttpsUrl(value) || isValidIpv4(value)
    }

    private fun isValidHttpsUrl(value: String): Boolean =
        try {
            val uri = URI(value)
            uri.scheme == "https" && !uri.host.isNullOrBlank()
        } catch (e: Exception) {
            false
        }

    private fun isValidIpv4(value: String): Boolean =
        try {
            InetAddress.getByName(value) is Inet4Address
        } catch (e: Exception) {
            false
        }
}
