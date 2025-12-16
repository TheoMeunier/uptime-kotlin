package tmeunier.fr.http.exceptions.core

import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.Instant

@RegisterForReflection
data class ApiErrorResponse(
    val error: String,
    val message: String,
    val details: String? = null,
    val timestamp: String = Instant.now().toString(),
    val path: String? = null,
    val traceId: String? = null
)