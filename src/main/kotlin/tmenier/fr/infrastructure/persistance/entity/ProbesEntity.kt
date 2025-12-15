package tmenier.fr.infrastructure.persistance.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import tmenier.fr.enums.HttpCodeEnum
import tmenier.fr.enums.ProbeProtocol
import tmenier.fr.infrastructure.persistance.mapper.HttpStatusCodeConverter
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "probes")
class ProbesEntity : PanacheEntityBase {
    @Id
    @Column(nullable = false)
    lateinit var id: UUID

    @Column(nullable = false, length = 255)
    lateinit var name: String

    @Column(nullable = false, length = 255)
    lateinit var url: String

    @Column(nullable = false)
    var interval: Int = 0

    @Column(nullable = false)
    var timeout: Int = 0

    @Column(name = "retry", nullable = false)
    var retry: Int = 0

    @Column(name = "interval_retry", nullable = false)
    var intervalRetry: Int = 0

    @Column(nullable = false)
    var enabled: Boolean = false

    @Column(nullable = false)
    var protocol: ProbeProtocol = ProbeProtocol.HTTP

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    // --- HTTP ---
    @Column(name = "notification_certified", nullable = false)
    var notificationCertified: Boolean = false

    @Column(name = "ignore_certificate_errors", nullable = false)
    var ignoreCertificateErrors: Boolean = false

    @Column(name = "http_code_allowed")
    @Convert(converter = HttpStatusCodeConverter::class)
    var httpCodeAllowed: List<HttpCodeEnum> = emptyList()

    // --- TCP ---
    @Column(name = "tcp_port")
    var tcpPort: Int? = null

    // --- DNS ---
    @Column(name = "dns_port")
    var dnsPort: Int? = null

    // --- PING ---
    @Column(name = "ping_max_packet")
    var pingMaxPacket: Int? = null

    @Column(name = "ping_size")
    var pingSize: Int? = null

    @Column(name = "ping_delay")
    var pingDelay: Int? = null

    @Column(name = "ping_numeric_output")
    var pingNumericOutput: Boolean? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime
}