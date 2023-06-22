package catmoe.fallencrystal.moefilter.network.bungee.handler

import catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher.handle
import catmoe.fallencrystal.moefilter.network.bungee.util.exception.InvalidUsernameException
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import lombok.RequiredArgsConstructor
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.protocol.DefinedPacket
import net.md_5.bungee.protocol.PacketWrapper
import net.md_5.bungee.protocol.packet.LoginRequest
import net.md_5.bungee.protocol.packet.PluginMessage

@RequiredArgsConstructor
class PacketHandler(playerHandler: PlayerHandler) : ChannelDuplexHandler() {
    @Suppress("OVERRIDE_DEPRECATION")
    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { handle(ctx.channel(), cause) }

    private val player = BungeeCord.getInstance().getPlayer(playerHandler.uniqueId)
    private val proxy = ProxyServer.getInstance()

    @Throws(Exception::class)
    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        if (msg is PluginMessage) {
            val pmTag = msg.tag
            if (pmTag.equals("mc|brand", ignoreCase = true) || pmTag.equals("minecraft:brand", ignoreCase = true)) {
                val backend: String
                val data = String(msg.data)
                backend = try { data.split(" <- ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1] } catch (ignore: Exception) { "unknown" }
                val brand = ByteBufAllocator.DEFAULT.heapBuffer()
                DefinedPacket.writeString("MoeFilter <- $backend", brand)
                msg.data = DefinedPacket.toArray(brand)
                brand.release()
            }
        }
        super.write(ctx, msg, promise)
    }

    @Throws(Exception::class)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is PacketWrapper) {
            val packet: Any? = msg.packet
            run {
                if (packet == null) { return@run }
                if (packet is LoginRequest) {
                    val username = (msg.packet as LoginRequest).data
                    if (username.isEmpty()) { throw InvalidUsernameException(ctx.channel().remoteAddress().toString() + "try to login but they username is empty.") }
                    // TODO Kick player here
                }
            }
        }
        super.channelRead(ctx, msg)
    }
}
