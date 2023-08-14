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

package catmoe.fallencrystal.moefilter.network.common.kick

import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import catmoe.fallencrystal.moefilter.network.common.kick.ServerKickType.BUNGEECORD
import catmoe.fallencrystal.moefilter.network.common.kick.ServerKickType.MOELIMBO
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.PacketDisconnect
import catmoe.fallencrystal.moefilter.util.message.component.ComponentUtil
import com.github.benmanes.caffeine.cache.Caffeine
import io.netty.channel.Channel
import net.kyori.adventure.text.Component
import net.md_5.bungee.protocol.packet.Kick

object FastDisconnect {
    private val reasonCache = Caffeine.newBuilder().build<DisconnectType, DisconnectReason>()

    private fun getPlaceholders(): Map<String, String> {
        val placeholderConfig = LocalConfig.getMessage().getConfig("kick.placeholders")
        val resultMap: MutableMap<String, String> = mutableMapOf()
        for (key in placeholderConfig.root().keys) { resultMap[LocalConfig.getMessage().getString("kick.placeholder-pattern").replace("[placeholder]", key)]=placeholderConfig.getString(key) }
        return resultMap
    }

    fun disconnect(channel: Channel, type: DisconnectType, platform: ServerKickType) {
        val p = (reasonCache.getIfPresent(type) ?: getCacheReason(type, ComponentUtil.parse("<red>Unknown kick reason: ${type.messagePath}"))).packet
        val packet: Any = when (platform) {
            BUNGEECORD -> p.bungeecord
            MOELIMBO -> p.moelimbo
        }
        channel.writeAndFlush(packet); channel.close()
    }

    fun disconnect(connection: ConnectionUtil, type: DisconnectType) {
        if (connection.isConnected) {
            val packet = (reasonCache.getIfPresent(type) ?: getCacheReason(type, ComponentUtil.parse("<red>Unknown kick reason: ${type.messagePath}"))).packet.bungeecord
            connection.writePacket(packet); connection.close()
        }
    }

    fun initMessages() {
        val placeholder = getPlaceholders()
        for (type in DisconnectType.values()) {
            // <newline> is MiniMessage's syntax. use it instead of \n
            val message = ComponentUtil.parse(replacePlaceholder(LocalConfig.getMessage().getStringList(type.messagePath).joinToString("<reset><newline>"), placeholder), true)
            reasonCache.put(type, getCacheReason(type, message))
        }
    }

    private fun replacePlaceholder(message: String, placeholder: Map<String, String>): String { var output = message; placeholder.forEach { output=output.replace(it.key, it.value) }; return output }

    private fun getCacheReason(type: DisconnectType, component: Component): DisconnectReason {
        val cs = ComponentUtil.toGson(component)
        val ml = PacketDisconnect()
        ml.setReason(cs)
        return DisconnectReason(type, component, KickPacket(Kick(cs), ml))
    }
}