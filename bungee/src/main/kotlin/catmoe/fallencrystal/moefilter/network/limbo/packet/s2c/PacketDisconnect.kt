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
import catmoe.fallencrystal.moefilter.network.limbo.compat.nbtchat.NbtMessage
import catmoe.fallencrystal.moefilter.network.limbo.compat.nbtchat.NbtMessageUtil
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboS2CPacket
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import catmoe.fallencrystal.translation.utils.version.Version
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.chat.BaseComponent

@Suppress("unused")
class PacketDisconnect(
    var message: NbtMessage? = null
) : LimboS2CPacket() {

    constructor(message: BaseComponent) : this(NbtMessageUtil.create(message))
    constructor(message: Component) : this(NbtMessageUtil.create(message))
    constructor(message: String) : this(NbtMessageUtil.create(message))

    fun setMessage(message: String) { this.message = NbtMessageUtil.create(message) }
    fun setMessage(baseComponent: BaseComponent) { this.message = NbtMessageUtil.create(baseComponent) }
    fun setMessage(component: Component) { this.message = NbtMessageUtil.create(component) }
    override fun encode(packet: ByteMessage, version: Version?) {
        val message = this.message ?: NbtMessageUtil.create(ComponentUtil.parse("null"))
        if (version?.moreOrEqual(Version.V1_20_2) == true) packet.writeNamelessCompoundTag(message.tag) else packet.writeString(message.json)
    }

    override fun toString() = "PacketDisconnect(message=$message)"
}