package tmenier.fr.monitors.requests

import com.fasterxml.jackson.annotation.JsonSubTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.monitors.ProbeProtocol

class StoreProbeRequestSubtypesTest {
    private val subtypes = BaseStoreProbeRequest::class.java.getAnnotation(JsonSubTypes::class.java).value

    @Test
    fun `every protocol has a request type`() {
        assertEquals(ProbeProtocol.entries.map { it.value }.sorted(), subtypes.map { it.name }.sorted())
    }

    @Test
    fun `every request class is registered once`() {
        val registered = subtypes.map { it.value }

        assertEquals(registered.size, registered.toSet().size, "a request class is registered twice")
        assertEquals(BaseStoreProbeRequest::class.sealedSubclasses.toSet(), registered.toSet())
    }
}
