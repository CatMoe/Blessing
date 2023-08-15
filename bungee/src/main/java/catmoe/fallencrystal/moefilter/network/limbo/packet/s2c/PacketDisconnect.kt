/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package catmoe.fallencrystal.moefilter.network.limbo.packet.s2c

import catmoe.fallencrystal.moefilter.network.limbo.netty.ByteMessage
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