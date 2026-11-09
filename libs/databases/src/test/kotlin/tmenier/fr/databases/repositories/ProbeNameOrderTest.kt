package tmenier.fr.databases.repositories

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProbeNameOrderTest {
    @Test
    fun `sorts names alphabetically regardless of case`() {
        val names = listOf("backup", "Zabbix", "api", "Website", "Cache")

        assertEquals(
            listOf("api", "backup", "Cache", "Website", "Zabbix"),
            names.sortedWith(ProbeNameOrder.NAMES),
        )
    }

    @Test
    fun `keeps a stable order for names differing only by case`() {
        assertEquals(
            listOf("API", "Api", "api"),
            listOf("api", "API", "Api").sortedWith(ProbeNameOrder.NAMES),
        )
    }

    @Test
    fun `sorts objects through a name selector`() {
        data class Probe(
            val name: String,
        )

        val probes = listOf(Probe("redis"), Probe("Postgres"), Probe("mongo"))

        assertEquals(
            listOf("mongo", "Postgres", "redis"),
            probes.sortedWith(ProbeNameOrder.by { it.name }).map { it.name },
        )
    }
}
