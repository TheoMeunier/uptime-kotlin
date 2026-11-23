package tmenier.fr.common.utils

import java.security.MessageDigest

object Sha256 {
    fun hex(value: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
