package catmoe.fallencrystal.moefilter.network.bungee.handler

import catmoe.fallencrystal.moefilter.common.check.info.impl.Pinging
import catmoe.fallencrystal.moefilter.common.check.mixed.MixedCheck
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.IPipeline
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.IPipeline.Companion.LAST_PACKET_INTERCEPTOR
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.IPipeline.Companion.PACKET_INTERCEPTOR
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.MoeChannelHandler
import catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher.handle
import catmoe.fallencrystal.moefilter.network.bungee.util.exception.InvalidHandshakeStatusException
import catmoe.fallencrystal.moefilter.network.bungee.util.exception.InvalidStatusPingException
import catmoe.fallencrystal.moefilter.network.bungee.util.exception.PacketOutOfBoundsException
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.config.ListenerInfo
import net.md_5.bungee.connection.InitialHandler
import net.md_5.bungee.netty.ChannelWrapper
import net.md_5.bungee.netty.PipelineUtils
import net.md_5.bungee.protocol.PacketWrapper
import net.md_5.bungee.protocol.packet.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean

class MoeInitialHandler(
    private val ctx: ChannelHandlerContext,
    listenerInfo: ListenerInfo,
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

    private var superHandshake = AtomicBoolean(true)

    @Throws(Exception::class)
    override fun handle(handshake: Handshake) {
        if (currentState !== ConnectionState.HANDSHAKE) { throw InvalidHandshakeStatusException("") }
        currentState = ConnectionState.PROCESSING
        currentState = when (handshake.requestedProtocol) {
            1 -> { ConnectionState.STATUS }
            2 -> { ConnectionState.JOINING }
            else -> { throw InvalidHandshakeStatusException("Invalid handshake protocol ${handshake.requestedProtocol}") }
        }
        pipeline!!.addBefore(PipelineUtils.BOSS_HANDLER, PACKET_INTERCEPTOR, PacketHandler())
        pipeline!!.addLast(LAST_PACKET_INTERCEPTOR, MoeChannelHandler.EXCEPTION_HANDLER)
        if (superHandshake.get()) { try { super.handle(handshake) } catch (exception: Exception) { exception.printStackTrace(); ctx.channel().close() } }
    }

    private var hasRequestedPing = false
    private var hasSuccessfullyPinged = false
    @Throws(Exception::class)
    override fun handle(statusRequest: StatusRequest) {
        if (hasRequestedPing || hasSuccessfullyPinged || currentState !== ConnectionState.STATUS) { throw InvalidStatusPingException() }
        hasRequestedPing = true
        currentState = ConnectionState.PROCESSING
        /*
        Call StatusRequest asynchronously.
        Some foolish pings tool always disconnects immediately after pinging.
        However, The vanilla client will not disconnect until it receives the returned ping measurement.

        Some other plugins (e.g. Protocolize, Triton) need to be injected into the netty pipeline.
        If they are disconnected before super.handle(statusRequest), A NoSuchElementException will throw here.
        But they are actually safe to ignore, I don't want console spam.
         */
       CompletableFuture.runAsync {
           if (!isConnected) { throw InvalidStatusPingException() }
           currentState = ConnectionState.PINGING
           hasSuccessfullyPinged = true
           MixedCheck.increase(Pinging(inetAddress ?: return@runAsync))
           try { super.handle(statusRequest) } catch (_: NoSuchElementException) {} // Actually inject netty failed.
        }
    }

    @Throws(Exception::class)
    override fun handle(ping: PingPacket) {
        // BungeeCord will accepts ping packets without StatusRequest packet. and throw a foolish exception.
        if (currentState !== ConnectionState.PINGING || !hasRequestedPing || !hasSuccessfullyPinged) { throw InvalidStatusPingException() }
        currentState = ConnectionState.PROCESSING
        // If we want more compatibility. Can use method super.handle(ping).
        // But in fact, the way it closes the connection is strange. So I chose to process directly here and close the pipeline for better performance.
        unsafe().sendPacket(ping)
        ctx.close()
    }

    @Throws(Exception::class)
    override fun handle(loginRequest: LoginRequest) {
        if (currentState !== ConnectionState.JOINING) { throw InvalidHandshakeStatusException("") }
        super.handle(loginRequest)
    }

    private var uniqueId: UUID? = null

    override fun setUniqueId(uuid: UUID?) {
        this.uniqueId=uuid

        // Bypass cannot modify premium user's uniqueId limit.
        val parent = InitialHandler::class.java
        val field = parent.getDeclaredField("uniqueId")
        field.isAccessible=true
        field.set(parent, uniqueId)
    }

    // Override = always use moefilter myself initialHandler's uniqueId field.
    // If that is null, super.getUniqueId will be used.
    override fun getUniqueId(): UUID? { return uniqueId ?: super.getUniqueId() }

    override fun handle(encryptResponse: EncryptionResponse?) {
        if (encryptResponse == null) return
        super.handle(encryptResponse)
    }

    override fun toString(): String {
        // I want to customize my own message insteadof replacing these with {0} and {1} placeholders.
        // return "§7(§f" + socketAddress + (if (name != null) "|$name" else "") + "§7) <-> MoeFilter InitialHandler"
        return "§7(§f$socketAddress${if (name != null) "|$name" else ""}§7) <-> MoeFilter InitialHandler"
    }
}
