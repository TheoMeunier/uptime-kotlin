package tmenier.fr.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import org.minidns.dnsmessage.DnsMessage
import org.minidns.dnsmessage.Question
import org.minidns.dnsqueryresult.StandardDnsQueryResult
import org.minidns.record.A
import org.minidns.record.AAAA
import org.minidns.record.CNAME
import org.minidns.record.Data
import org.minidns.record.MX
import org.minidns.record.TXT
import org.minidns.source.NetworkDataSource
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.monitors.dtos.propbes.ProbeContent
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import tmenier.fr.monitors.enums.ProbeProtocol
import tmenier.fr.monitors.enums.RecordDnsEnum
import java.net.InetAddress
import java.net.URI

@ApplicationScoped
class ProbeProtocolDns : ProbeProtocolAbstract<ProbeContent.Dns>() {
    override fun execute(
        probe: ProbesEntity,
        content: ProbeContent.Dns,
        isLastAttempt: Boolean,
    ): ProbeResult {
        val start = now()

        return try {
            val dnsServerAddress = InetAddress.getByName(content.dnsServer)

            val customDataSource =
                object : NetworkDataSource() {
                    override fun query(
                        message: DnsMessage,
                        address: InetAddress,
                        port: Int,
                    ): StandardDnsQueryResult? = super.query(message, dnsServerAddress, content.dnsPort)
                }

            val recordType =
                when (content.recordType ?: RecordDnsEnum.A) {
                    RecordDnsEnum.A -> java.lang.Record.TYPE.A
                    RecordDnsEnum.AAAA -> java.lang.Record.TYPE.AAAA
                    RecordDnsEnum.CNAME -> java.lang.Record.TYPE.CNAME
                    RecordDnsEnum.MX -> java.lang.Record.TYPE.MX
                    RecordDnsEnum.TXT -> java.lang.Record.TYPE.TXT
                }

            val hostname =
                try {
                    URI(content.hostname).host ?: content.hostname
                } catch (e: Exception) {
                    content.hostname
                        .removePrefix("https://")
                        .removePrefix("http://")
                        .trimEnd('/')
                }

            val question = Question(hostname, recordType)
            val rawMessage =
                DnsMessage
                    .builder()
                    .setQuestion(question)
                    .setRecursionDesired(true)
                    .build()

            val result =
                customDataSource.query(rawMessage, dnsServerAddress, content.dnsPort)
                    ?: throw Exception("No response from DNS server")

            val answerRecords =
                result.response.answerSection
                    .filter { it.type == recordType }

            if (answerRecords.isEmpty()) {
                throw Exception("No ${content.recordType} record found for ${content.hostname}")
            }

            val description = buildRecordDescription(content.recordType ?: RecordDnsEnum.A, answerRecords)

            ProbeResult(
                status = ProbeMonitorLogStatus.SUCCESS,
                responseTime = getResponseTime(start),
                message = "DNS lookup successful: $description in ${getResponseTime(start)} ms",
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

    private fun buildRecordDescription(
        recordType: RecordDnsEnum,
        records: List<java.lang.Record<out Data>>,
    ): String =
        when (recordType) {
            RecordDnsEnum.A -> {
                val aRecords = records.filter { it.type == java.lang.Record.TYPE.A }
                if (aRecords.isEmpty()) throw Exception("No A record found")
                "${aRecords.size} A record(s): ${aRecords.joinToString { (it.payloadData as A).toString() }}"
            }

            RecordDnsEnum.AAAA -> {
                val aaaaRecords = records.filter { it.type == java.lang.Record.TYPE.AAAA }
                if (aaaaRecords.isEmpty()) throw Exception("No AAAA record found")
                "${aaaaRecords.size} AAAA record(s): ${aaaaRecords.joinToString { (it.payloadData as AAAA).toString() }}"
            }

            RecordDnsEnum.CNAME -> {
                val cnameRecords = records.filter { it.type == java.lang.Record.TYPE.CNAME }
                if (cnameRecords.isEmpty()) throw Exception("No CNAME record found — l'apex domain (ex: example.com) ne peut pas avoir de CNAME")
                "${cnameRecords.size} CNAME record(s): ${cnameRecords.joinToString { (it.payloadData as CNAME).target.toString() }}"
            }

            RecordDnsEnum.MX -> {
                val mxRecords = records.filter { it.type == java.lang.Record.TYPE.MX }
                if (mxRecords.isEmpty()) throw Exception("No MX record found")
                val sorted = mxRecords.sortedBy { (it.payloadData as MX).priority }
                "${sorted.size} MX record(s): ${
                    sorted.joinToString {
                        val mx = it.payloadData as MX
                        "${mx.target} (priority: ${mx.priority})"
                    }
                }"
            }

            RecordDnsEnum.TXT -> {
                val txtRecords = records.filter { it.type == java.lang.Record.TYPE.TXT }
                if (txtRecords.isEmpty()) throw Exception("No TXT record found")
                "${txtRecords.size} TXT record(s): ${
                    txtRecords.joinToString {
                        "\"${(it.payloadData as TXT).text}\""
                    }
                }"
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
