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

package catmoe.fallencrystal.moefilter.network.common.kick

import catmoe.fallencrystal.moefilter.common.counter.ConnectionStatistics
import catmoe.fallencrystal.moefilter.data.BlockType
import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import catmoe.fallencrystal.moefilter.network.common.ServerType
import catmoe.fallencrystal.moefilter.network.common.ServerType.*
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.ExplicitPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.PacketDisconnect
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable
import com.github.benmanes.caffeine.cache.Caffeine
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import net.kyori.adventure.text.Component
import net.md_5.bungee.protocol.packet.Kick

object FastDisconnect : Reloadable {
    val reasonCache = Caffeine.newBuilder().build<DisconnectType, DisconnectReason>()
    private val cachedByteArray = Caffeine.newBuilder().build<DisconnectReason, ByteArray>()

    private fun getPlaceholders(): Map<String, String> {
        val placeholderConfig = LocalConfig.getMessage().getConfig("kick.placeholders")
        val resultMap: MutableMap<String, String> = mutableMapOf()
        for (key in placeholderConfig.root().keys) { resultMap[LocalConfig.getMessage().getString("kick.placeholder-pattern").replace("[placeholder]", key)]=placeholderConfig.getString(key) }
        return resultMap
    }

    fun disconnect(channel: Channel, type: DisconnectType, platform: ServerType) {
        val p = (reasonCache.getIfPresent(type) ?: getCacheReason(type, ComponentUtil.parse("<red>Unknown kick reason: ${type.messagePath}"))).packet
        val packet: Any = when (platform) {
            BUNGEE_CORD -> p.bungeecord
            MOE_LIMBO -> p.moelimbo
            VELOCITY -> throw UnsupportedOperationException("Velocity support soon.")
        }
        channel.writeAndFlush(packet); channel.close()
        ConnectionStatistics.countBlocked(BlockType.JOIN)
    }

    fun disconnect(connection: ConnectionUtil, type: DisconnectType) {
        if (connection.isConnected) {
            val packet = (reasonCache.getIfPresent(type) ?: getCacheReason(type, ComponentUtil.parse("<red>Unknown kick reason: ${type.messagePath}"))).packet.bungeecord
            connection.writePacket(packet); connection.close()
            ConnectionStatistics.countBlocked(BlockType.JOIN)
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
            ConnectionStatistics.countBlocked(BlockType.JOIN)
        }
    }

    override fun reload() {
        this.initMessages()
    }

    @Suppress("EnumValuesSoftDeprecate")
    fun initMessages() {
        val placeholder = getPlaceholders()
        for (type in DisconnectType.values()) {
            // <newline> is MiniMessage's syntax. use it instead of \n
            val message = ComponentUtil.parse(replacePlaceholder(
                LocalConfig.getMessage().getStringList("kick.${type.messagePath}")
                    .joinToString("<reset><newline>"), placeholder),
                true
            )
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

        return DisconnectReason(type, cs, KickPacket(Kick(cs), ExplicitPacket(0x00, array, "Cached kick packet (type=${type.name})")), component)
    }
}