package com.ryuta46.nemapitestandroid.util

import java.io.ByteArrayOutputStream

class ConvertUtil {
    companion object {
        fun toHexString(bytes : ByteArray): String {
            val result = StringBuffer()
            bytes.forEach {
                result.append(String.format("%02x",it))
            }
            return result.toString()
        }

        fun toByteArray(s: String): ByteArray {
            val len = s.length
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                val upper = Character.digit(s[i], 16) shl 4
                val lower = Character.digit(s[i + 1], 16)
                data[i / 2] = ( lower + upper).toByte()
                i += 2
            }
            return data
        }

        fun swapByteArray(bytes: ByteArray): ByteArray {
            val ret =  ByteArray(bytes.size)
            for(i in 0 until bytes.size) {
                ret[ret.size - i - 1] =  bytes[i]
            }
            return ret
        }

        fun toByteArrayWithLittleEndian(value: Int): ByteArray {
            val ret = ByteArray(4)
            for (i in 0..3) {
                ret[i] = (value shr (i * 8) and 0xFF).toByte()
            }
            return ret
        }

        fun toByteArrayWithLittleEndian(value: Long): ByteArray {
            val ret = ByteArray(8)
            for (i in 0..7) {
                ret[i] = (value shr (i * 8) and 0xFF).toByte()
            }
            return ret
        }
    }
}