package tmenier.fr.databases.converts

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Convert
import tmenier.fr.monitors.enums.HttpCodeEnum

@Convert
class HttpStatusCodeConverter : AttributeConverter<List<HttpCodeEnum>, String> {
    override fun convertToDatabaseColumn(attribute: List<HttpCodeEnum>?): String? =
        attribute
            ?.joinToString(",") { it.value }

    override fun convertToEntityAttribute(dbData: String?): List<HttpCodeEnum>? =
        dbData
            ?.split(",")
            ?.mapNotNull { raw ->
                val normalized = raw.trim().replace("–", "-")
                HttpCodeEnum.entries.firstOrNull { it.value == normalized }
            }
}
