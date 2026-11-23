package tmenier.fr.common.utils

import java.io.ByteArrayOutputStream

object Base32 {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    private const val BITS_PER_CHAR = 5
    private const val BITS_PER_BYTE = 8
    private const val MASK = 0x1f

    fun encode(bytes: ByteArray): String {
        val output = StringBuilder((bytes.size * BITS_PER_BYTE + BITS_PER_CHAR - 1) / BITS_PER_CHAR)
        var buffer = 0
        var bits = 0

        for (byte in bytes) {
            buffer = (buffer shl BITS_PER_BYTE) or (byte.toInt() and 0xff)
            bits += BITS_PER_BYTE
            while (bits >= BITS_PER_CHAR) {
                output.append(ALPHABET[(buffer shr (bits - BITS_PER_CHAR)) and MASK])
                bits -= BITS_PER_CHAR
            }
        }
        if (bits > 0) output.append(ALPHABET[(buffer shl (BITS_PER_CHAR - bits)) and MASK])

        return output.toString()
    }

    fun decode(text: String): ByteArray {
        val clean = text.trim().trimEnd('=').replace(" ", "").uppercase()
        val output = ByteArrayOutputStream(clean.length * BITS_PER_CHAR / BITS_PER_BYTE)
        var buffer = 0
        var bits = 0

        for (char in clean) {
            val value = ALPHABET.indexOf(char)
            require(value >= 0) { "Invalid base32 character" }
            buffer = (buffer shl BITS_PER_CHAR) or value
            bits += BITS_PER_CHAR
            if (bits >= BITS_PER_BYTE) {
                output.write((buffer shr (bits - BITS_PER_BYTE)) and 0xff)
                bits -= BITS_PER_BYTE
            }
        }

        return output.toByteArray()
    }
}
