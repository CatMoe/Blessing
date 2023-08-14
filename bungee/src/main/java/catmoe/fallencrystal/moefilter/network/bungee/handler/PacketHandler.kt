package catmoe.fallencrystal.moefilter.network.bungee.handler

import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.channel.ClientBrandPostEvent
import catmoe.fallencrystal.moefilter.common.check.brand.BrandCheck
import catmoe.fallencrystal.moefilter.common.check.info.impl.Brand
import catmoe.fallencrystal.moefilter.common.check.info.impl.Joining
import catmoe.fallencrystal.moefilter.common.check.misc.*
import catmoe.fallencrystal.moefilter.common.check.mixed.MixedCheck
import catmoe.fallencrystal.moefilter.common.check.name.similarity.SimilarityCheck
import catmoe.fallencrystal.moefilter.common.check.name.valid.ValidNameCheck
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.counter.type.BlockType
import catmoe.fallencrystal.moefilter.network.bungee.util.PipelineUtil
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher.handle
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidUsernameException
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType.*
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.network.common.kick.ServerKickType
import catmoe.fallencrystal.moefilter.common.utils.component.ComponentUtil
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
import net.md_5.bungee.protocol.packet.*
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@RequiredArgsConstructor
class PacketHandler : ChannelDuplexHandler() {
    @Suppress("OVERRIDE_DEPRECATION")
    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { handle(ctx.channel(), cause) }

    val protocol = AtomicInteger(0)

    private val proxy = ProxyServer.getInstance()

    val cancelled = AtomicBoolean(false)
    val isAvailable = AtomicBoolean(false)

    @Throws(Exception::class)
    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        isAvailable.set(true)
        if (msg is PluginMessage && LocalConfig.getConfig().getBoolean("f3-brand.enabled")) {
            val pmTag = msg.tag
            if (pmTag.equals("mc|brand", ignoreCase = true) || pmTag.equals("minecraft:brand", ignoreCase = true)) {
                val backend: String
                val data = String(msg.data)
                backend = try { data.split(" <- ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1] } catch (ignore: Exception) { "unknown" }
                val brand = ByteBufAllocator.DEFAULT.heapBuffer()
                val target = LocalConfig.getConfig().getString("f3-brand.custom")
                    .replace("%bungee%", proxy.name)
                    .replace("%version%", proxy.version)
                    .replace("%backend%", ComponentUtil.componentToRaw(ComponentUtil.legacyToComponent(backend)))
                DefinedPacket.writeString((MessageUtil.colorize(target)).toLegacyText(), brand)
                msg.data = DefinedPacket.toArray(brand)
                brand.release()
            }
        }
        super.write(ctx, msg, promise)
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val channel = ctx.channel()
        val inetSocketAddress = channel.remoteAddress() as InetSocketAddress
        if (msg is PacketWrapper) {
            val packet: Any? = msg.packet
            run {
                if (packet == null) { return@run }
                if (packet is LoginRequest) {
                    val username = packet.data
                    if (username.isEmpty()) { throw InvalidUsernameException(channel.remoteAddress().toString() + "try to login but they username is empty.") }
                    if (check(channel, inetSocketAddress, username)) { ConnectionCounter.countBlocked(BlockType.JOIN); return }
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
                        if (BrandCheck.increase(Brand(clientBrand))) { kick(channel, BRAND_NOT_ALLOWED) }
                    }
                }
                // if (packet is KeepAlive) { MessageUtil.logInfo("[MoeFilter] [KeepAlive] id: ${packet.randomId} address: ${ctx.channel().remoteAddress()} Client -> Server") }
            }
        }
        super.channelRead(ctx, msg)
    }

    private fun check(channel: Channel, inetSocketAddress: InetSocketAddress, name: String): Boolean {
        val inetAddress = inetSocketAddress.address
        val protocol = this.protocol.get()
        val joining = Joining(name, inetAddress, protocol)
        if (ValidNameCheck.instance.increase(joining)) { kick(channel, INVALID_NAME); return true }
        val mixinKick = MixedCheck.increase(joining)
        if (mixinKick != null) { kick(channel, mixinKick); return true }
        if (SimilarityCheck.instance.increase(Joining(name, inetAddress, protocol))) { kick(channel, INVALID_NAME); return true }
        if (AlreadyOnlineCheck().increase(Joining(name, inetAddress, protocol))) { kick(channel, ALREADY_ONLINE); return true }
        return false
    }

    private fun kick(channel: Channel, type: DisconnectType) {
        FastDisconnect.disconnect(channel, type, ServerKickType.BUNGEECORD)
        cancelled.set(true)
    }
}
