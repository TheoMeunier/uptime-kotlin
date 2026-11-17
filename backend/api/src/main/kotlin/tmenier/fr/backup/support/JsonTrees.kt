package tmenier.fr.backup.support

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode

object JsonTrees {
    val factory: JsonNodeFactory = JsonNodeFactory.instance

    private val snakeCase = PropertyNamingStrategies.SnakeCaseStrategy()

    fun copy(node: JsonNode): ObjectNode = node.deepCopy()

    fun array(values: Iterable<String>): ArrayNode = factory.arrayNode().apply { values.forEach { add(it) } }

    fun toSnakeCase(node: JsonNode): JsonNode =
        when (node) {
            is ObjectNode ->
                factory.objectNode().also { copy ->
                    node.properties().forEach { (key, value) -> copy.set<JsonNode>(snakeCase.translate(key), toSnakeCase(value)) }
                }

            is ArrayNode -> factory.arrayNode().also { copy -> node.forEach { copy.add(toSnakeCase(it)) } }

            else -> node
        }

    fun mergeMissing(
        target: ObjectNode,
        source: ObjectNode,
    ) {
        source.properties().forEach { (key, value) -> if (!target.has(key)) target.set<JsonNode>(key, value) }
    }

    fun replaceTexts(
        node: JsonNode,
        transform: (String) -> String?,
    ) {
        when (node) {
            is ObjectNode ->
                node.properties().map { it.key to it.value }.forEach { (key, child) ->
                    if (child.isTextual) transform(child.asText())?.let { node.put(key, it) } else replaceTexts(child, transform)
                }

            is ArrayNode ->
                (0 until node.size()).forEach { index ->
                    val child = node.get(index)
                    if (child.isTextual) transform(child.asText())?.let { node.set(index, factory.textNode(it)) } else replaceTexts(child, transform)
                }
        }
    }

    fun ObjectNode.isAbsent(field: String): Boolean = path(field).let { it.isNull || it.isMissingNode }
}
