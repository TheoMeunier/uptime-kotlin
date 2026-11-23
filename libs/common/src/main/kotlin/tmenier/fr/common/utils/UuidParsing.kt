package tmenier.fr.common.utils

import java.util.UUID

fun String?.toUuidOrNull(): UUID? = this?.let { runCatching { UUID.fromString(it) }.getOrNull() }
