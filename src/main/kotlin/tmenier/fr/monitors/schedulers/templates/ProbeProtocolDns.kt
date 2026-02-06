package tmenier.fr.monitors.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import org.minidns.DnsClient
import org.minidns.dnsmessage.DnsMessage
import org.minidns.dnsqueryresult.StandardDnsQueryResult
import org.minidns.record.A
import org.minidns.record.Record
import org.minidns.source.NetworkDataSource
import tmenier.fr.monitors.dtos.propbes.ProbeContent
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import tmenier.fr.monitors.enums.ProbeProtocol
import tmenier.fr.monitors.schedulers.dto.ProbeResult
import java.net.InetAddress

@ApplicationScoped
class ProbeProtocolDns : ProbeProtocolAbstract<ProbeContent.Dns>() {
    override fun execute(
        probe: ProbesEntity,
        content: ProbeContent.Dns,
        isLastAttempt: Boolean,
    ): ProbeResult {
        val start = now()

        return try {
            val customDataSource =
                object : NetworkDataSource() {
                    private val dnsServerAddress = InetAddress.getByName(content.dnsServer)

                    override fun query(
                        message: DnsMessage,
                        address: InetAddress,
                        port: Int,
                    ): StandardDnsQueryResult? = super.query(message, dnsServerAddress, content.dnsPort)
                }

            val dnsClient =
                DnsClient().apply {
                    setDataSource(customDataSource)
                }

            val result = dnsClient.query(content.hostname, Record.TYPE.A)

            if (!result.wasSuccessful()) {
                throw Exception("DNS query failed: ${result.response.responseCode}")
            }

            val aRecords = result.response.answerSection.filterIsInstance<Record<A>>()

            if (aRecords.isEmpty()) {
                throw Exception("No A record found for ${content.hostname}")
            }

            ProbeResult(
                status = ProbeMonitorLogStatus.SUCCESS,
                responseTime = getResponseTime(start),
                message = "DNS lookup successful: ${aRecords.size} A record(s) found in ${getResponseTime(start)} ms",
                runAt = getRunAt(start),
            )
        } catch (e: Exception) {
            ProbeResult(
                status = getStatus(isLastAttempt, probe),
                responseTime = getResponseTime(start),
                message = "DNS lookup failed: ${e.message}",
                runAt = getRunAt(start),
            )
        }
    }

    override fun getProtocolType() = ProbeProtocol.DNS.name

    private fun getStatus(
        isLastAttempt: Boolean,
        probe: ProbesEntity,
    ): ProbeMonitorLogStatus =
        when {
            isLastAttempt -> ProbeMonitorLogStatus.FAILURE
            probe.status == ProbeMonitorLogStatus.FAILURE -> ProbeMonitorLogStatus.FAILURE
            else -> ProbeMonitorLogStatus.WARNING
        }
}
