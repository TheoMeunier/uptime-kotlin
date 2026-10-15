package tmenier.fr.incidents

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import tmenier.fr.deployments.ApiOnlyProfile
import tmenier.fr.incidents.actions.ListIncidentsAction
import java.time.Duration
import java.time.Instant

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class ListIncidentsActionTest {
    @Inject
    lateinit var action: ListIncidentsAction

    @Test
    fun `invalid filters are refused instead of being ignored`() {
        assertThrows(BadRequestException::class.java) { list(state = "resolved") }
        assertThrows(BadRequestException::class.java) { list(probeId = "probe-1") }
        assertThrows(BadRequestException::class.java) { list(from = "2026-10-01") }
        assertThrows(BadRequestException::class.java) { list(from = "2026-10-02T00:00:00Z", to = "2026-10-01T00:00:00Z") }
        assertThrows(BadRequestException::class.java) { list(page = -1) }
        assertThrows(BadRequestException::class.java) { list(size = ListIncidentsAction.MAX_PAGE_SIZE + 1) }
    }

    @Test
    fun `the state filter ignores case`() {
        list(state = "OPEN")
        list(state = "closed")
    }

    @Test
    fun `the summary covers the last 30 days by default`() {
        val now = Instant.parse("2026-10-15T12:00:00Z")

        val summary = action.summary(probeId = null, from = null, to = null, now = now)

        assertEquals(now, summary.to)
        assertEquals(now.minus(Duration.ofDays(30)), summary.from)
    }

    private fun list(
        state: String? = null,
        probeId: String? = null,
        from: String? = null,
        to: String? = null,
        page: Int = 0,
        size: Int = 20,
    ) = action.execute(state, probeId, from, to, page, size)
}
