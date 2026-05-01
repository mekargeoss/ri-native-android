/**
 * 2026 Mekarge OSS and Maintainers
 * Licensed under the MIT License. See LICENSE file in the project root
 * for full license information.
 */

package oss.mekarge.a3.nat.android

object Ecdsa {
    fun derToJose(der: ByteArray, size: Int): ByteArray {
        if (der.isEmpty() || der[0] != 0x30.toByte()) throw IllegalArgumentException("Not DER SEQUENCE")
        var idx = 2
        if (der[1].toInt() and 0x80 != 0) {
            val n = der[1].toInt() and 0x7F
            idx = 2 + n
        }
        if (der[idx] != 0x02.toByte()) throw IllegalArgumentException("Expected INTEGER r")
        val rLen = der[idx + 1].toInt()
        val rBytes = der.copyOfRange(idx + 2, idx + 2 + rLen)
        idx += 2 + rLen
        if (der[idx] != 0x02.toByte()) throw IllegalArgumentException("Expected INTEGER s")
        val sLen = der[idx + 1].toInt()
        val sBytes = der.copyOfRange(idx + 2, idx + 2 + sLen)

        val r = leftPad(stripLeadingZeros(rBytes), size)
        val s = leftPad(stripLeadingZeros(sBytes), size)
        return r + s
    }

    private fun stripLeadingZeros(b: ByteArray): ByteArray {
        var i = 0
        while (i < b.size - 1 && b[i] == 0x00.toByte()) i++
        return b.copyOfRange(i, b.size)
    }

    private fun leftPad(b: ByteArray, size: Int): ByteArray {
        if (b.size == size) return b
        if (b.size > size) return b.copyOfRange(b.size - size, b.size)
        val out = ByteArray(size)
        System.arraycopy(b, 0, out, size - b.size, b.size)
        return out
    }
}
