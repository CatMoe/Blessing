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
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer

@Suppress("unused")
class PacketDisconnect : LimboS2CPacket() {

    var message: String = ""

    fun setReason(message: String) { this.message=message }

    fun setReason(baseComponent: BaseComponent) { this.message=ComponentSerializer.toString(baseComponent) }

    fun setReason(component: Component) { this.message= ComponentUtil.toGson(component) }

    override fun encode(packet: ByteMessage, version: Version?) { packet.writeString(message) }

    override fun toString(): String { return "PacketDisconnect(message=$message)" }
}