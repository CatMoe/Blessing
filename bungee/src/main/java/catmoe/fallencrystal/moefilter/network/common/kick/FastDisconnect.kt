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

import catmoe.fallencrystal.moefilter.common.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.counter.type.BlockType
import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import catmoe.fallencrystal.moefilter.network.common.kick.ServerKickType.BUNGEECORD
import catmoe.fallencrystal.moefilter.network.common.kick.ServerKickType.MOELIMBO
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.netty.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.ExplicitPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.PacketDisconnect
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import com.github.benmanes.caffeine.cache.Caffeine
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import net.kyori.adventure.text.Component
import net.md_5.bungee.protocol.packet.Kick

object FastDisconnect {
    private val reasonCache = Caffeine.newBuilder().build<DisconnectType, DisconnectReason>()
    private val cachedByteArray = Caffeine.newBuilder().build<DisconnectReason, ByteArray>()

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
        ConnectionCounter.countBlocked(BlockType.JOIN)
    }

    fun disconnect(connection: ConnectionUtil, type: DisconnectType) {
        if (connection.isConnected) {
            val packet = (reasonCache.getIfPresent(type) ?: getCacheReason(type, ComponentUtil.parse("<red>Unknown kick reason: ${type.messagePath}"))).packet.bungeecord
            connection.writePacket(packet); connection.close()
            ConnectionCounter.countBlocked(BlockType.JOIN)
        }
    }

    fun disconnect(handler: LimboHandler, type: DisconnectType) {
        if (!handler.disconnected.get()) {
            val cs = reasonCache.getIfPresent(type) ?: return
            when (handler.state) {
                Protocol.PLAY -> {
                    val packet = PacketDisconnect()
                    packet.setReason(cs.raw)
                    handler.sendPacket(packet)
                }
                else -> handler.sendPacket(cs.packet.moelimbo)
            }
            handler.channel.close()
            ConnectionCounter.countBlocked(BlockType.JOIN)
        }
    }

    @Suppress("EnumValuesSoftDeprecate")
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
        /*
        Limbo Packets:
         */
        val kick = PacketDisconnect()
        kick.setReason(cs)
        val ba = ByteMessage(Unpooled.buffer())
        kick.encode(ba, null)
        val array = ba.toByteArray()
        ba.release()
        // End

        return DisconnectReason(type, cs, KickPacket(Kick(cs), ExplicitPacket(0x00, array, "Cached kick packet")))
    }
}