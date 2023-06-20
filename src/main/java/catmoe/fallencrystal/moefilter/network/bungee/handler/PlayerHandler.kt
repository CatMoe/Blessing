package catmoe.fallencrystal.moefilter.network.bungee.handler

import catmoe.fallencrystal.moefilter.common.utils.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher.handle
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.IPipeline
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.MoeChannelHandler
import catmoe.fallencrystal.moefilter.network.bungee.util.exception.InvalidHandshakeStatusException
import catmoe.fallencrystal.moefilter.network.bungee.util.exception.InvalidStatusPingException
import catmoe.fallencrystal.moefilter.network.bungee.util.exception.PacketOutOfBoundsException
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.config.ListenerInfo
import net.md_5.bungee.connection.InitialHandler
import net.md_5.bungee.netty.ChannelWrapper
import net.md_5.bungee.protocol.PacketWrapper
import net.md_5.bungee.protocol.packet.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture

class PlayerHandler(
    private val ctx: ChannelHandlerContext,
    listenerInfo: ListenerInfo?,
) : InitialHandler(BungeeCord.getInstance(), listenerInfo), IPipeline {
    private var currentState = ConnectionState.HANDSHAKE
    private var inetAddress: InetAddress? = null
    private var pipeline: ChannelPipeline? = null
    @Throws(Exception::class)
    override fun connected(wrapper: ChannelWrapper) {
        super.connected(wrapper)
        pipeline = wrapper.handle.pipeline()
        inetAddress = (socketAddress as InetSocketAddress).address
    }

    @Throws(Exception::class)
    override fun exception(t: Throwable) { handle(ctx.channel(), t) }

    @Throws(Exception::class)
    override fun handle(packet: PacketWrapper?) {
        if (packet == null) { throw PacketOutOfBoundsException("Null packet") }
        if (packet.buf.readableBytes() > 2048) {
            packet.buf.clear()
            throw PacketOutOfBoundsException("Packet" + packet.buf.readableBytes() + "is out of bounds")
        }
    }

    @Throws(Exception::class)
    override fun handle(handshake: Handshake) {
        if (currentState !== ConnectionState.HANDSHAKE) { throw InvalidHandshakeStatusException("") }
        currentState = ConnectionState.PROCESSING
        currentState = when (handshake.requestedProtocol) {
            1 -> { ConnectionState.STATUS }
            2 -> { ConnectionState.JOINING }
            else -> { throw InvalidHandshakeStatusException("Invalid handshake protocol ${handshake.requestedProtocol}") }
        }

        pipeline!!.addLast(IPipeline.LAST_PACKET_INTERCEPTOR, MoeChannelHandler.EXCEPTION_HANDLER)
        super.handle(handshake)
    }

    private var hasRequestedPing = false
    private var hasSuccessfullyPinged = false
    @Throws(Exception::class)
    override fun handle(statusRequest: StatusRequest) {
        if (hasRequestedPing || hasSuccessfullyPinged || currentState !== ConnectionState.STATUS) { throw InvalidStatusPingException() }
        hasRequestedPing = true
        currentState = ConnectionState.PROCESSING
        CompletableFuture.runAsync {
            if (!isConnected) { throw InvalidStatusPingException() }
            currentState = ConnectionState.PINGING
            hasSuccessfullyPinged = true
            try { super.handle(statusRequest) } catch (exception: Exception) { throw InvalidStatusPingException() }
        }
    }

    @Throws(Exception::class)
    override fun handle(ping: PingPacket) {
        if (currentState !== ConnectionState.PINGING || !hasRequestedPing || !hasSuccessfullyPinged) { throw InvalidStatusPingException() }
        currentState = ConnectionState.PROCESSING
        unsafe().sendPacket(ping)
        ctx.close()
    }

    @Throws(Exception::class)
    override fun handle(loginRequest: LoginRequest) {
        inetAddress?.let { ConnectionCounter.increase(it) }
        if (currentState !== ConnectionState.JOINING) { throw InvalidHandshakeStatusException("") }
    }

    override fun toString(): String {
        // return "§7(§f" + socketAddress + (if (name != null) "|$name" else "") + "§7) <-> MoeFilter InitialHandler"
        return "§7(§f$socketAddress${if (name != null) "|$name" else ""}§7) <-> MoeFilter InitialHandler"
    }
}
