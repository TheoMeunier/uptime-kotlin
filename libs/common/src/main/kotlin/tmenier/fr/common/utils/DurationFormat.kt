package tmenier.fr.common.utils

import java.time.Duration

fun Duration.toHumanReadable(): String {
    val totalSeconds = this.seconds

    val days = totalSeconds / 86_400
    val hours = (totalSeconds % 86_400) / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60

    return buildString {
        if (days > 0) append("${days}d ")
        if (hours > 0) append("${hours}h ")
        if (minutes > 0) append("${minutes}m ")
        if (days == 0L && seconds > 0) append("${seconds}s")
    }.trim().ifEmpty { "0s" }
}
