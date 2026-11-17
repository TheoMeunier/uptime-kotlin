package tmenier.fr.backup.services

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.validation.Validator

sealed interface RequestRead<out R> {
    data class Valid<R>(
        val request: R,
    ) : RequestRead<R>

    data class Invalid(
        val errors: List<String>,
    ) : RequestRead<Nothing>
}

@ApplicationScoped
class RequestReader(
    private val objectMapper: ObjectMapper,
    private val validator: Validator,
) {
    fun <R : Any> read(
        item: JsonNode,
        type: Class<R>,
        groups: Array<Class<*>>,
    ): RequestRead<R> {
        val request =
            try {
                objectMapper.treeToValue(item, type)
            } catch (e: JsonProcessingException) {
                return RequestRead.Invalid(listOf(e.originalMessage.substringBefore('\n')))
            } catch (e: IllegalArgumentException) {
                return RequestRead.Invalid(listOf(e.message ?: "Invalid value"))
            }

        val violations = validator.validate(request, *groups).map { "${it.propertyPath}: ${it.message}" }.sorted()
        return if (violations.isEmpty()) RequestRead.Valid(request) else RequestRead.Invalid(violations)
    }

    fun <R : Any> require(
        item: JsonNode,
        type: Class<R>,
    ): R = objectMapper.treeToValue(item, type)
}
