package tmenier.fr.common.providers

import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ParamConverter
import jakarta.ws.rs.ext.ParamConverterProvider
import jakarta.ws.rs.ext.Provider
import tmenier.fr.common.exceptions.core.ApiErrorResponse
import java.lang.reflect.Type
import java.util.UUID

@Provider
class UuidParamConverterProvider : ParamConverterProvider {
    override fun <T> getConverter(
        rawType: Class<T>,
        genericType: Type?,
        annotations: Array<out Annotation>?,
    ): ParamConverter<T>? {
        if (rawType != UUID::class.java) {
            return null
        }

        @Suppress("UNCHECKED_CAST")
        return UuidParamConverter as ParamConverter<T>
    }
}

object UuidParamConverter : ParamConverter<UUID> {
    private val UUID_FORMAT =
        Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

    override fun fromString(value: String?): UUID? {
        if (value == null) {
            return null
        }

        if (!UUID_FORMAT.matches(value)) {
            throw malformedUuid()
        }

        return runCatching { UUID.fromString(value) }.getOrElse { throw malformedUuid() }
    }

    override fun toString(value: UUID?): String? = value?.toString()

    private fun malformedUuid(): WebApplicationException =
        WebApplicationException(
            Response
                .status(Response.Status.BAD_REQUEST)
                .entity(
                    ApiErrorResponse(
                        error = "BAD_REQUEST",
                        message = "Invalid identifier: a UUID is expected.",
                    ),
                ).type(MediaType.APPLICATION_JSON)
                .build(),
        )
}
