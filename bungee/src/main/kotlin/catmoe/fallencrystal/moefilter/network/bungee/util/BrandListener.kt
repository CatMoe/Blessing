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

package catmoe.fallencrystal.moefilter.network.bungee.util

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.moefilter.check.brand.BrandCheck
import catmoe.fallencrystal.moefilter.check.info.impl.Brand
import catmoe.fallencrystal.moefilter.event.packet.BungeeKnownPacketEvent
import catmoe.fallencrystal.moefilter.event.packet.BungeePacketEvent
import catmoe.fallencrystal.moefilter.event.packet.PacketAction
import catmoe.fallencrystal.moefilter.event.packet.PacketDirection
import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import catmoe.fallencrystal.moefilter.network.common.ServerType
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.translation.event.EventListener
import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.event.annotations.EventHandler
import catmoe.fallencrystal.translation.event.annotations.HandlerPriority
import catmoe.fallencrystal.translation.event.events.player.PlayerPostBrandEvent
import catmoe.fallencrystal.translation.player.PlayerInstance
import catmoe.fallencrystal.translation.player.bungee.BungeePlayer
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import catmoe.fallencrystal.translation.utils.config.IgnoreInitReload
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.buffer.Unpooled
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.protocol.DefinedPacket
import net.md_5.bungee.protocol.packet.PluginMessage

@IgnoreInitReload
object BrandListener : EventListener, Reloadable {

    private val proxy = MoeFilterBungee.instance.proxy

    private var modifyBrand = LocalConfig.getConfig().getBoolean("f3-brand.enabled")

    override fun reload() {
        modifyBrand = LocalConfig.getConfig().getBoolean("f3-brand.enabled")
    }

    @EventHandler(BungeeKnownPacketEvent::class, priority = HandlerPriority.LOWEST)
    fun handleBrand(event: BungeeKnownPacketEvent) {
        val packet = event.packet
        val version = event.version
        if (event.isCancelled() || packet !is PluginMessage || version.less(Version.V1_8) || event.direction != PacketDirection.UPSTREAM) return
        val tag = packet.tag
        if ((tag == "MC|Brand" && version.less(Version.V1_13) || (tag == "minecraft:brand" && version.moreOrEqual(Version.V1_13)))) {
            val username = event.initialHandler.name
            val channel = event.ctx.channel()
            when (event.action) {
                PacketAction.READ -> {
                    try {
                        if (version.less(Version.V1_8)) return
                        val buf = Unpooled.wrappedBuffer(packet.data)
                        val clientBrand = DefinedPacket.readString(buf)
                        buf.release()
                        if (clientBrand.isEmpty() || clientBrand.length > 128) { channel.close(); return }
                        if (BrandCheck.increase(Brand(clientBrand))) {
                            FastDisconnect.disconnect(channel, DisconnectType.BRAND_NOT_ALLOWED, ServerType.BUNGEE_CORD)
                            event.setCancelled()
                            return
                        }
                        val translatePlayer = PlayerInstance.getPlayer(event.initialHandler.uniqueId) ?: return
                        (translatePlayer.upstream as BungeePlayer).clientBrand=clientBrand
                        EventManager.callEvent(PlayerPostBrandEvent(translatePlayer, clientBrand))
                    } catch (exception: Exception) {
                        MessageUtil.logWarn("Caught exception when reading brand: ${exception.localizedMessage}")
                        if (ExceptionCatcher.debug) exception.printStackTrace()
                    }
                }
                PacketAction.WRITE -> {
                    if (!modifyBrand) return
                    val data = String(packet.data)
                    val backend = try { data.split(" <- ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1] } catch (ignore: Exception) { "unknown" }
                    val target = LocalConfig.getConfig().getString("f3-brand.custom")
                        .replace("%bungee%", proxy.name)
                        .replace("%version%", proxy.version)
                        .replace("%backend%", ComponentUtil.componentToRaw(ComponentUtil.legacyToComponent(backend)))
                    val player: ProxiedPlayer? = proxy.getPlayer(username)
                    val brand = (MessageUtil.colorize(target, false)).toLegacyText()
                    if ((player?.pendingConnection?.version ?: 0) >= 47) {
                        val buf = ByteMessage.create()
                        buf.writeString(brand)
                        packet.data = buf.toByteArray()
                        buf.release()
                    }
                }
            }
        }
    }

    @EventHandler(BungeePacketEvent::class, priority = HandlerPriority.LOWEST)
    fun handleLegacyBrand(event: BungeePacketEvent) {
        // Find a way to improve this
        val version = event.version
        if (
            version.moreOrEqual(Version.V1_8) ||
            event.id != 0x17 ||
            event.direction != PacketDirection.UPSTREAM ||
            event.action != PacketAction.READ
        ) return
        val byteBuf = event.byteBuf
        try {
            byteBuf.readVarInt() // Packet id. Ignore that.
            val tag = byteBuf.readString()
            if (tag != "MC|Brand") return
            val player = PlayerInstance.getPlayer(event.initialHandler.uniqueId) ?: return
            fun readBytes17(): Int {
                var low = byteBuf.readUnsignedShort()
                var high = 0
                if (low and 0x8000 != 0) {
                    low = low and 0x7FFF
                    high = byteBuf.readUnsignedByte().toInt()
                }
                return high and 0xFF shl 15 or low
            }
            val brandBytes = ByteMessage(byteBuf.readRetainedSlice(readBytes17()))
            val brand = brandBytes.readString()
            brandBytes.release()
            val channel = event.ctx.channel()
            if (brand.isEmpty() || brand.length > 128) { channel.close(); return }
            if (BrandCheck.increase(Brand(brand))) {
                FastDisconnect.disconnect(channel, DisconnectType.BRAND_NOT_ALLOWED, ServerType.BUNGEE_CORD)
                event.setCancelled()
                return
            }
            (player.upstream as BungeePlayer).clientBrand=brand
            EventManager.callEvent(PlayerPostBrandEvent(player, brand))
        } finally {
            byteBuf.release()
        }
    }

}