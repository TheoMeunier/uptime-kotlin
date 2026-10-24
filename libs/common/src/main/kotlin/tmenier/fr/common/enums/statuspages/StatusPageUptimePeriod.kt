package tmenier.fr.common.enums.statuspages

import java.time.Duration

enum class StatusPageUptimePeriod(
    val bucket: Duration,
    val bucketCount: Int,
) {
    H1(Duration.ofMinutes(1), 60),
    D1(Duration.ofHours(1), 24),
    D7(Duration.ofHours(4), 42),
    D30(Duration.ofDays(1), 30),
    D60(Duration.ofDays(1), 60),
    D90(Duration.ofDays(1), 90),
    ;

    val span: Duration get() = bucket.multipliedBy(bucketCount.toLong())

    val aggregated: Boolean get() = this != H1
}
