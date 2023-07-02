package catmoe.fallencrystal.moefilter.network.bungee.handler

import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.channel.ClientBrandPostEvent
import catmoe.fallencrystal.moefilter.common.check.already_online.AlreadyOnlineCheck
import catmoe.fallencrystal.moefilter.common.check.info.impl.Joining
import catmoe.fallencrystal.moefilter.common.check.mixed.MixedCheck
import catmoe.fallencrystal.moefilter.common.check.valid_name.ValidNameCheck
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher.handle
import catmoe.fallencrystal.moefilter.network.bungee.util.PipelineUtil
import catmoe.fallencrystal.moefilter.network.bungee.util.exception.InvalidUsernameException
import catmoe.fallencrystal.moefilter.network.bungee.util.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.bungee.util.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import lombok.RequiredArgsConstructor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.protocol.DefinedPacket
import net.md_5.bungee.protocol.PacketWrapper
import net.md_5.bungee.protocol.packet.LoginRequest
import net.md_5.bungee.protocol.packet.PluginMessage
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicBoolean

@RequiredArgsConstructor
class PacketHandler : ChannelDuplexHandler() {
    @Suppress("OVERRIDE_DEPRECATION")
    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { handle(ctx.channel(), cause) }

    private val proxy = ProxyServer.getInstance()

    val cancelled = AtomicBoolean(false)
    val isAvailable = AtomicBoolean(false)

    @Throws(Exception::class)
    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        isAvailable.set(true)
        if (msg is PluginMessage) {
            val pmTag = msg.tag
            if (pmTag.equals("mc|brand", ignoreCase = true) || pmTag.equals("minecraft:brand", ignoreCase = true)) {
                val backend: String
                val data = String(msg.data)
                backend = try { data.split(" <- ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1] } catch (ignore: Exception) { "unknown" }
                val brand = ByteBufAllocator.DEFAULT.heapBuffer()
                val target = LocalConfig.getConfig().getString("f3-brand")
                    .replace("%bungee%", proxy.name)
                    .replace("%version%", proxy.version)
                    .replace("%backend%", backend)
                DefinedPacket.writeString((MessageUtil.colorize(target)).toLegacyText(), brand)
                msg.data = DefinedPacket.toArray(brand)
                brand.release()
            }
        }
        super.write(ctx, msg, promise)
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val inetAddress = (ctx.channel().remoteAddress() as InetSocketAddress).address
        val channel = ctx.channel()
        if (msg is PacketWrapper) {
            val packet: Any? = msg.packet
            run {
                if (packet == null) { return@run }
                if (packet is LoginRequest) {
                    val username = packet.data
                    if (username.isEmpty()) { throw InvalidUsernameException(channel.remoteAddress().toString() + "try to login but they username is empty.") }
                    if (!ValidNameCheck.instance.increase(Joining(username, inetAddress))) { kick(channel, DisconnectType.INVALID_NAME); return }
                    val mixinKick = MixedCheck.increase(Joining(username, inetAddress))
                    if (mixinKick != null) { kick(channel, mixinKick); return }
                    if (!AlreadyOnlineCheck().increase(Joining(username, inetAddress))) { kick(channel, DisconnectType.ALREADY_ONLINE); return }
                    // TODO More kick here.
                    PipelineUtil.putChannelHandler(ctx, username)
                }
                if (packet is PluginMessage) {
                    if (packet.tag == "MC|Brand" || packet.tag == "minecraft:brand") {
                        val player = PipelineUtil.getPlayer(ctx) ?: return
                        val brand = Unpooled.wrappedBuffer(packet.data)
                        val clientBrand = DefinedPacket.readString(brand)
                        brand.release()
                        if (clientBrand.isEmpty() || clientBrand.length > 128) { channel.close(); return }
                        EventManager.triggerEvent(ClientBrandPostEvent(channel, player, clientBrand))
                    }
                }
                // if (packet is KeepAlive) { MessageUtil.logInfo("[MoeFilter] [KeepAlive] id: ${packet.randomId} address: ${ctx.channel().remoteAddress()} Client -> Server") }
            }
        }
        super.channelRead(ctx, msg)
    }

    private fun kick(channel: Channel, type: DisconnectType) {
        FastDisconnect.disconnect(channel, type)
        cancelled.set(true)
    }
}
