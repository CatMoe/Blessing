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

package catmoe.fallencrystal.moefilter.network.bungee.handler

import catmoe.fallencrystal.moefilter.check.brand.BrandCheck
import catmoe.fallencrystal.moefilter.check.info.impl.Brand
import catmoe.fallencrystal.moefilter.network.common.ByteMessage
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import catmoe.fallencrystal.moefilter.network.common.ServerType
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.event.events.player.PlayerPostBrandEvent
import catmoe.fallencrystal.translation.player.PlayerInstance
import catmoe.fallencrystal.translation.player.bungee.BungeePlayer
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.connection.InitialHandler
import net.md_5.bungee.protocol.DefinedPacket
import net.md_5.bungee.protocol.PacketWrapper
import net.md_5.bungee.protocol.packet.PluginMessage


@Suppress("CanBeParameter")
class BasicPacketHandler(val username: String, private val initialHandler: InitialHandler) : ChannelDuplexHandler() {

    private val version = initialHandler.version

    override fun write(ctx: ChannelHandlerContext, msg: Any?, promise: ChannelPromise?) {
        if (msg is PluginMessage && LocalConfig.getConfig().getBoolean("f3-brand.enabled")) {
            val pmTag = msg.tag
            if (pmTag == "MC|Brand" || pmTag == "minecraft:brand" && version >= Version.V1_8.number) {
                val backend: String
                val data = String(msg.data)
                backend = try { data.split(" <- ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1] } catch (ignore: Exception) { "unknown" }
                val target = LocalConfig.getConfig().getString("f3-brand.custom")
                    .replace("%bungee%", proxy.name)
                    .replace("%version%", proxy.version)
                    .replace("%backend%", ComponentUtil.componentToRaw(ComponentUtil.legacyToComponent(backend)))
                val player: ProxiedPlayer? = proxy.getPlayer(username)
                val brand = (MessageUtil.colorize(target, false)).toLegacyText()
                if ((player?.pendingConnection?.version ?: 0) >= 47) {
                    val buf = ByteMessage.create()
                    buf.writeString(brand)
                    msg.data = buf.toByteArray()
                    buf.release()
                }
            }
        }
        super.write(ctx, msg, promise)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        val channel = ctx.channel()
        run {
            if (msg is ByteBuf) {
                val copy = msg.copy()
                val byteMessage = ByteMessage(copy)
                try {
                    // PluginMessage for 1.7. Some BungeeCord just not provide correctly bytes.
                    if (byteMessage.readVarInt() == 0x17 && version < Version.V1_8.number) {
                        val player = proxy.getPlayer(username) ?: return@run
                        val tag = byteMessage.readString()
                        if (tag != "MC|Brand") return@run
                        fun readBytes17(): Int {
                            var low = byteMessage.readUnsignedShort()
                            var high = 0
                            if (low and 0x8000 != 0) {
                                low = low and 0x7FFF
                                high = byteMessage.readUnsignedByte().toInt()
                            }
                            return high and 0xFF shl 15 or low
                        }
                        val brandBytes = ByteMessage(byteMessage.readRetainedSlice(readBytes17()))
                        val brand = brandBytes.readString()
                        if (brand.isEmpty() || brand.length > 128) { channel.close(); return }
                        brandBytes.release()
                        if (BrandCheck.increase(Brand(brand))) {
                            FastDisconnect.disconnect(channel, DisconnectType.BRAND_NOT_ALLOWED, ServerType.BUNGEE_CORD)
                            return@run
                        }
                        val translatePlayer = PlayerInstance.getPlayer(player.uniqueId) ?: return@run
                        (translatePlayer.upstream as BungeePlayer).clientBrand=brand
                        EventManager.callEvent(PlayerPostBrandEvent(translatePlayer, brand))
                    }
                } catch (exception: Exception) {
                    MessageUtil.logWarn("Caught exception when reading brand: ${exception.localizedMessage}")
                    if (ExceptionCatcher.debug) exception.printStackTrace()
                } finally {
                    byteMessage.release()
                }
            }
            if (msg is PacketWrapper) {
                val packet = msg.packet
                if (packet is PluginMessage && version >= Version.V1_8.number) {
                    val tag = packet.tag
                    if (tag == "MC|Brand" || tag == "minecraft:brand") {
                        try {
                            val player = proxy.getPlayer(username) ?: return@run
                            if (player.pendingConnection.version < 47) return
                            val buf = Unpooled.wrappedBuffer(packet.data)
                            val clientBrand = DefinedPacket.readString(buf)
                            buf.release()
                            if (clientBrand.isEmpty() || clientBrand.length > 128) { channel.close(); return }
                            if (BrandCheck.increase(Brand(clientBrand))) {
                                FastDisconnect.disconnect(channel, DisconnectType.BRAND_NOT_ALLOWED, ServerType.BUNGEE_CORD)
                                return@run
                            }
                            val translatePlayer = PlayerInstance.getPlayer(player.uniqueId) ?: return@run
                            (translatePlayer.upstream as BungeePlayer).clientBrand=clientBrand
                            EventManager.callEvent(PlayerPostBrandEvent(translatePlayer, clientBrand))
                        } catch (exception: Exception) {
                            MessageUtil.logWarn("Caught exception when reading brand: ${exception.localizedMessage}")
                            if (ExceptionCatcher.debug) exception.printStackTrace()
                        }
                    }
                }
            }
        }
        super.channelRead(ctx, msg)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)
    }

    companion object {
        private val proxy = ProxyServer.getInstance()
    }

}