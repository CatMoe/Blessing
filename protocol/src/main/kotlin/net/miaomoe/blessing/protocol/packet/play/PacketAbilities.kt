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

package net.miaomoe.blessing.protocol.packet.play

import net.miaomoe.blessing.protocol.direction.PacketDirection
import net.miaomoe.blessing.protocol.packet.type.PacketBidirectional
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketAbilities @JvmOverloads constructor(
    var flags: Int = 0,
    var flySpeed: Float = 0f,
    var viewModifier: Float = flySpeed
) : PacketBidirectional {

    @JvmOverloads constructor(
        flagList: List<Flags>,
        flySpeed: Float = 0f,
        viewModifier: Float = flySpeed
    ) : this(0, flySpeed, viewModifier) {
        flagList.distinct().forEach { flags = flags or it.mask }
    }

    override fun encode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        when (direction) {
            PacketDirection.TO_CLIENT -> {
                byteBuf.writeByte(flags)
                byteBuf.writeFloat(flySpeed)
                byteBuf.writeFloat(viewModifier)
            }
            PacketDirection.TO_SERVER -> byteBuf.writeByte(flags)
        }
    }

    override fun decode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        when (direction) {
            PacketDirection.TO_CLIENT -> {
                this.flags = byteBuf.readByte().toInt()
                this.flySpeed = byteBuf.readFloat()
                this.viewModifier = byteBuf.readFloat()
            }
            PacketDirection.TO_SERVER -> this.flags = byteBuf.readByte().toInt()
        }
    }

    enum class Flags(val mask: Int) {
        INVULNERABLE(0x01),
        FLYING(0x02),
        ALLOW_FLYING(0x04),
        INSTANT_BREAK(0x08);

        companion object {
            @JvmStatic
            fun fromFlags(flags: Int): MutableList<Flags> {
                val decodedFlags = mutableListOf<Flags>()
                for (flag in Flags.entries) {
                    if (flags and flag.mask != 0) decodedFlags.add(flag)
                }
                return decodedFlags
            }
        }
    }

}