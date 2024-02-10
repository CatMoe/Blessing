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

import net.miaomoe.blessing.nbt.chat.MixedComponent
import net.miaomoe.blessing.protocol.direction.PacketDirection
import net.miaomoe.blessing.protocol.message.BossBar
import net.miaomoe.blessing.protocol.message.BossBarAction
import net.miaomoe.blessing.protocol.message.BossBarFlags
import net.miaomoe.blessing.protocol.message.Style
import net.miaomoe.blessing.protocol.packet.type.PacketBidirectional
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.version.Version
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class PacketBossBar(
    var uuid: UUID = UUID(0, 0),
    var action: BossBarAction<*> = BossBarAction.REMOVE,
    var value: Any = Unit
) : PacketBidirectional {

    override val forceDirection = PacketDirection.TO_CLIENT

    override fun encode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        byteBuf.writeUUID(uuid)
        byteBuf.writeVarInt(action.id)
        @Suppress("UNCHECKED_CAST")
        when (action) {
            is BossBarAction.Add -> {
                val value = this.value as BossBar
                byteBuf.writeChat(value.message, version)
                byteBuf.writeFloat(value.health)
                value.style.write(byteBuf)
                byteBuf.writeByte(BossBarFlags.toFlags(value.flags))
            }
            is BossBarAction.UpdateHealth -> byteBuf.writeFloat(value as Float)
            is BossBarAction.UpdateTitle -> byteBuf.writeChat(value as MixedComponent, version)
            is BossBarAction.UpdateStyle -> (value as Style).write(byteBuf)
            is BossBarAction.UpdateFlags -> byteBuf.writeByte(BossBarFlags.toFlags(value as List<BossBarFlags>))
        }
    }

    override fun decode(byteBuf: ByteMessage, version: Version, direction: PacketDirection) {
        this.uuid = byteBuf.readUUID()
        val actionId = byteBuf.readVarInt()
        this.action = BossBarAction.entries.firstOrNull { it.id == actionId } ?: throw IllegalArgumentException("Unsupported action id: $actionId")
        when (action) {
            is BossBarAction.Add -> {
                this.value = BossBar(
                    byteBuf.readChat(version),
                    byteBuf.readFloat(),
                    Style.read(byteBuf),
                    BossBarFlags.fromFlag(byteBuf.readByte().toInt())
                )
            }
            is BossBarAction.UpdateHealth -> this.value = byteBuf.readFloat()
            is BossBarAction.UpdateTitle -> this.value = byteBuf.readChat(version)
            is BossBarAction.UpdateStyle -> this.value = Style.read(byteBuf)
            is BossBarAction.UpdateFlags -> BossBarFlags.fromFlag(byteBuf.readByte().toInt())
        }
    }

}