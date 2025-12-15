package tmenier.fr.infrastructure.persistance.mapper

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import tmenier.fr.enums.HttpCodeEnum


@Converter
class HttpStatusCodeConverter :
    AttributeConverter<List<HttpCodeEnum>, String> {

    override fun convertToDatabaseColumn(attribute: List<HttpCodeEnum>?): String? {
        return attribute
            ?.joinToString(",") { it.value }
    }

    override fun convertToEntityAttribute(dbData: String?): List<HttpCodeEnum>? {
        return dbData
            ?.split(",")
            ?.mapNotNull { raw ->
                val normalized = raw.trim().replace("–", "-")
                HttpCodeEnum.entries.firstOrNull { it.value == normalized }
            }
    }
}
