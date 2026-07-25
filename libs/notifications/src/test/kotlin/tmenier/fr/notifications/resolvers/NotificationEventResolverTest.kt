package tmenier.fr.notifications.resolvers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.notifications.NotificationEvent

class NotificationEventResolverTest {
    private val resolver = NotificationEventResolver()

    @Test
    fun `only confirmed failure and recovery transitions notify`() {
        val expected =
            mapOf(
                (ProbeMonitorLogStatus.SUCCESS to ProbeMonitorLogStatus.WARNING) to NotificationEvent.NONE,
                (ProbeMonitorLogStatus.WARNING to ProbeMonitorLogStatus.WARNING) to NotificationEvent.NONE,
                (ProbeMonitorLogStatus.WARNING to ProbeMonitorLogStatus.SUCCESS) to NotificationEvent.NONE,
                (ProbeMonitorLogStatus.WARNING to ProbeMonitorLogStatus.FAILURE) to NotificationEvent.FAILURE,
                (ProbeMonitorLogStatus.SUCCESS to ProbeMonitorLogStatus.FAILURE) to NotificationEvent.FAILURE,
                (ProbeMonitorLogStatus.FAILURE to ProbeMonitorLogStatus.FAILURE) to NotificationEvent.NONE,
                (ProbeMonitorLogStatus.FAILURE to ProbeMonitorLogStatus.SUCCESS) to NotificationEvent.RECOVERY,
            )

        expected.forEach { (transition, event) ->
            assertEquals(event, resolver.resolve(transition.first, transition.second))
        }
    }
}
