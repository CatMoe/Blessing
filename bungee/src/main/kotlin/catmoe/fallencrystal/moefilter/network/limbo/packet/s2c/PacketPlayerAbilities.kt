/*
 * Copyright (C) 2023-2023. CatMoe / MoeFilter Contributors
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

package catmoe.fallencrystal.moefilter.network.limbo.packet.s2c

import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.translation.utils.version.Version

@Suppress("MemberVisibilityCanBePrivate")
class PacketPlayerAbilities(
    var flags: Int = Flags.DEFAULT.id,
    var flyingSpeed: Float = 0f,
    var walkSpeed: Float = 0.1f
) : LimboS2CPacket() {

    constructor(
        flags: Flags,
        flyingSpeed: Float = 0f,
        walkSpeed: Float = 0.1f
    ) : this(flags.id, flyingSpeed, walkSpeed)

    override fun encode(byteBuf: ByteMessage, version: Version?) {
        byteBuf.writeByte(flags)
        //listOf(flyingSpeed, fieldOfView).forEach { packet.writeFloat(it) }
        byteBuf.writeFloat(flyingSpeed)
        byteBuf.writeFloat(walkSpeed)
    }

    override fun toString() = "PacketPlayerAbilities(flags=$flags, flyingSpeed=$flyingSpeed, fieldOfView=$walkSpeed)"

    enum class Flags(val id: Int) {
        DEFAULT(0x00),
        FLYING(0x02)
    }

}