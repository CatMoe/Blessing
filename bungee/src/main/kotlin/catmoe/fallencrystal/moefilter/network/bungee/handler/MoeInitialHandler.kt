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

import catmoe.fallencrystal.moefilter.check.info.impl.Address
import catmoe.fallencrystal.moefilter.check.info.impl.Pinging
import catmoe.fallencrystal.moefilter.common.check.misc.CountryCheck
import catmoe.fallencrystal.moefilter.common.check.misc.DomainCheck
import catmoe.fallencrystal.moefilter.common.check.misc.ProxyCheck
import catmoe.fallencrystal.moefilter.common.check.mixed.MixedCheck
import catmoe.fallencrystal.moefilter.common.counter.ConnectionStatistics
import catmoe.fallencrystal.moefilter.data.BlockType
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.IPipeline
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.IPipeline.Companion.LAST_PACKET_INTERCEPTOR
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.MoeChannelHandler
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher.handle
import catmoe.fallencrystal.moefilter.network.common.ServerType
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidHandshakeException
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidStatusPingException
import catmoe.fallencrystal.moefilter.network.common.exception.PacketOutOfBoundsException
import catmoe.fallencrystal.moefilter.network.common.kick.DisconnectType
import catmoe.fallencrystal.moefilter.network.common.kick.FastDisconnect
import io.netty.channel.Channel
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

// Borrowed from :
// https://github.com/SpigotMC/BungeeCord/blob/master/proxy/src/main/java/net/md_5/bungee/connection/InitialHandler.java
@Suppress("MemberVisibilityCanBePrivate")
class MoeInitialHandler(
    private val ctx: ChannelHandlerContext,
    listenerInfo: ListenerInfo
) : InitialHandler(BungeeCord.getInstance(), listenerInfo), IPipeline {
    private val channel = ctx.channel()
    private var currentState = ConnectionState.HANDSHAKE
    private var inetSocketAddress: InetSocketAddress? = null
    private var inetAddress: InetAddress? = null
    private var pipeline: ChannelPipeline? = null
    private val packetHandler = BungeePacketHandler()
    @Throws(Exception::class)
    override fun connected(wrapper: ChannelWrapper) {
        super.connected(wrapper)
        this.pipeline = wrapper.handle.pipeline()
        this.inetSocketAddress = socketAddress as InetSocketAddress
        this.packetHandler.inetSocketAddress= this.inetSocketAddress!!
        this.inetAddress = this.inetSocketAddress!!.address
    }

    @Throws(Exception::class)
    override fun exception(t: Throwable) { handle(channel, t) }

    @Throws(Exception::class)
    override fun handle(packet: PacketWrapper?) {
        if (packet == null) { throw PacketOutOfBoundsException("Null packet") }
        if (packet.buf.readableBytes() > 2048) {
            packet.buf.clear()
            throw PacketOutOfBoundsException("Packet" + packet.buf.readableBytes() + "is out of bounds")
        }
    }

    private var superHandshake = AtomicBoolean(true)

    @JvmField
    var handshake: Handshake? = null
        //get() = field ?: super.getHandshake()

    override fun getHandshake(): Handshake {
        return handshake ?: super.getHandshake()
    }

    @Throws(Exception::class)
    override fun handle(handshake: Handshake) {
        if (currentState !== ConnectionState.HANDSHAKE) { throw InvalidHandshakeException("") }
        currentState = ConnectionState.PROCESSING
        currentState = when (handshake.requestedProtocol) {
            1 -> { ConnectionState.STATUS }
            2 -> {
                val info = Address(inetSocketAddress!!, InetSocketAddress(handshake.host, handshake.port))
                if (DomainCheck.instance.increase(info)) { kick(channel, DisconnectType.INVALID_HOST); return }
                if (CountryCheck().increase(info)) { kick(channel, DisconnectType.COUNTRY); return }
                if (ProxyCheck().increase(info)) { kick(channel, DisconnectType.PROXY); return }
                ConnectionState.JOINING
            }
            else -> { throw InvalidHandshakeException("Invalid handshake protocol ${handshake.requestedProtocol}") }
        }
        handshake.host=checkHost(handshake.host)
        this.handshake=handshake
        pipeline!!.addBefore(PipelineUtils.BOSS_HANDLER, IPipeline.PACKET_INTERCEPTOR, packetHandler)
        packetHandler.protocol.set(handshake.protocolVersion)
        pipeline!!.addLast(LAST_PACKET_INTERCEPTOR, MoeChannelHandler.EXCEPTION_HANDLER)
        if (superHandshake.get()) { try { super.handle(handshake) } catch (exception: Exception) { exception.printStackTrace(); channel.close() } }
        MoeChannelHandler.sentHandshake.put(channel, true)
    }

    private fun checkHost(host: String): String {
        val h = host.replace("FML$".toRegex(), "").replace("\\.$".toRegex(), "")
        if (host.length >= 256 || host.isEmpty() || host == "0" /* Prevent EndMinecraftPlus ping ——They host is still 0 */)
            throw InvalidHandshakeException("Host length check failed")
        return h
    }

    private var hasRequestedPing = false
    private var hasSuccessfullyPinged = false
    @Throws(Exception::class)
    override fun handle(statusRequest: StatusRequest) {
        if (hasRequestedPing || hasSuccessfullyPinged || currentState !== ConnectionState.STATUS) {
            ConnectionStatistics.countBlocked(BlockType.PING)
            throw InvalidStatusPingException()
        }
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
           if (!isConnected) { ConnectionStatistics.countBlocked(BlockType.PING); throw InvalidStatusPingException() }
           currentState = ConnectionState.PINGING
           hasSuccessfullyPinged = true
           MixedCheck.increase(Pinging((inetAddress ?: return@runAsync), packetHandler.protocol.get()))
           try { super.handle(statusRequest) } catch (_: NoSuchElementException) {} catch (_: NullPointerException) {} // Actually inject netty failed.
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
        if (currentState !== ConnectionState.JOINING) { throw InvalidHandshakeException("") }
        name=loginRequest.data
        super.handle(loginRequest)
        this.loginRequest=loginRequest
    }

    @JvmField
    var uniqueId: UUID? = null
    @JvmField
    var loginRequest: LoginRequest? = null
    @JvmField
    var name: String? = null

    override fun getLoginRequest(): LoginRequest? { return loginRequest ?: super.getLoginRequest() }

    override fun setUniqueId(uuid: UUID?) {
        this.uniqueId=uuid

        // Bypass cannot modify premium user's uniqueId limit.
        val parent = InitialHandler::class.java
        val field = parent.getDeclaredField("uniqueId")
        field.isAccessible=true
        field[parent] = uniqueId
    }

    // Override = always use moefilter myself initialHandler's uniqueId field.
    // If that is null, super.getUniqueId will be used.
    override fun getUniqueId(): UUID? { return uniqueId ?: super.getUniqueId() }

    override fun handle(encryptResponse: EncryptionResponse?) {
        if (encryptResponse == null) return
        super.handle(encryptResponse)
    }

    private fun kick(channel: Channel, type: DisconnectType) {
        FastDisconnect.disconnect(channel, type, ServerType.BUNGEE_CORD)
    }

    override fun toString(): String {
        // I want to customize my own message insteadof replacing these with {0} and {1} placeholders.
        // return "§7(§f" + socketAddress + (if (name != null) "|$name" else "") + "§7) <-> MoeFilter InitialHandler"
        return "§7(§f$socketAddress${if (name != null) "|$name" else ""}§7) <-> MoeFilter InitialHandler"
    }
}