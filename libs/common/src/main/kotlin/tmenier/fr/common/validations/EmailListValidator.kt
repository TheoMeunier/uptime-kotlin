package tmenier.fr.common.validations

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [EmailListValidator::class])
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class EmailList(
    val message: String = "Must be a comma-separated list of valid email addresses",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class EmailListValidator : ConstraintValidator<EmailList, String> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext,
    ): Boolean = value.isNullOrBlank() || isValidEmailList(value)

    companion object {
        private val SEPARATORS = Regex("[,;]")
        private val EMAIL = Regex("^[^\\s@,;<>]+@[^\\s@,;<>.]+(\\.[^\\s@,;<>.]+)+$")

        fun parse(value: String?): List<String> =
            value
                ?.split(SEPARATORS)
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                .orEmpty()

        fun isValidEmailList(value: String?): Boolean {
            val addresses = parse(value)
            return addresses.isNotEmpty() && addresses.all { EMAIL.matches(it) }
        }
    }
}
