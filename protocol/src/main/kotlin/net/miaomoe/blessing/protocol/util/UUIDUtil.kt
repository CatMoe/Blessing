/*
 * Copyright (C) 2023-2024. CatMoe / Blessing Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.miaomoe.blessing.protocol.util

import java.nio.charset.StandardCharsets
import java.util.*

object UUIDUtil {
    private const val MOJANG_BROKEN_UUID_LENGTH = 32
    @Suppress("SpellCheckingInspection")
    private const val HEX_DIGITS = "0123456789abcdef"

    fun parseLegacyUUID(uuidSequence: CharSequence): UUID {
        require(uuidSequence.length == MOJANG_BROKEN_UUID_LENGTH) { "Illegal UUID string: $uuidSequence" }
        var mostSignificantBits = 0L
        var leastSignificantBits = 0L
        for (i in 0 until MOJANG_BROKEN_UUID_LENGTH) {
            val c = uuidSequence[i]
            when (i) {
                in 0 until 16 -> mostSignificantBits = (mostSignificantBits shl 4) or HEX_DIGITS.indexOf(c).toLong()
                in 16 until 32 -> leastSignificantBits = (leastSignificantBits shl 4) or HEX_DIGITS.indexOf(c).toLong()
                else -> throw IllegalArgumentException("Illegal hexadecimal digit: $c")
            }
        }
        return UUID(mostSignificantBits, leastSignificantBits)
    }

    fun toLegacyFormat(uuid: UUID): String {
        val mostSignificantBits = uuid.mostSignificantBits
        val leastSignificantBits = uuid.leastSignificantBits
        val builder = StringBuilder(32)
        for (shift in 60 downTo 0 step 4) {
            builder.append(HEX_DIGITS[(mostSignificantBits shr shift).toInt() and 0xF])
            builder.append(HEX_DIGITS[(leastSignificantBits shr shift).toInt() and 0xF])
        }

        return builder.toString()
    }

    fun generateOfflinePlayerUuid(username: String): UUID =
        UUID.nameUUIDFromBytes("OfflinePlayer:$username".toByteArray(StandardCharsets.UTF_8))
}