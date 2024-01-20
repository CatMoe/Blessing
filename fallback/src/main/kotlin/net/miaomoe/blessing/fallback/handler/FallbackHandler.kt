/*
 * Copyright (C) 2023-2024. CatMoe / Blessing Contributors
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

package net.miaomoe.blessing.fallback.handler

import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.handler.codec.haproxy.HAProxyMessage
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder
import net.miaomoe.blessing.fallback.config.FallbackConfig
import net.miaomoe.blessing.protocol.packet.configuration.PacketFinishConfiguration
import net.miaomoe.blessing.protocol.packet.configuration.PacketRegistryData
import net.miaomoe.blessing.protocol.packet.handshake.PacketHandshake
import net.miaomoe.blessing.protocol.packet.login.PacketLoginAcknowledged
import net.miaomoe.blessing.protocol.packet.login.PacketLoginRequest
import net.miaomoe.blessing.protocol.packet.login.PacketLoginResponse
import net.miaomoe.blessing.protocol.packet.status.PacketStatusPing
import net.miaomoe.blessing.protocol.packet.status.PacketStatusRequest
import net.miaomoe.blessing.protocol.packet.status.PacketStatusResponse
import net.miaomoe.blessing.protocol.packet.type.PacketToClient
import net.miaomoe.blessing.protocol.registry.State
import net.miaomoe.blessing.protocol.util.UUIDUtil
import net.miaomoe.blessing.protocol.version.Version
import java.net.InetSocketAddress
import java.util.logging.Level

@Suppress("MemberVisibilityCanBePrivate")
class FallbackHandler(val channel: Channel) : ChannelInboundHandlerAdapter() {

    private val config = FallbackConfig.INSTANCE

    val encoder = FallbackEncoder(handler = this)
    val decoder = FallbackDecoder(handler = this)

    val pipeline: ChannelPipeline = channel.pipeline()

    private val validate = if (config.validate) ValidateHandler(this) else null

    var state = State.HANDSHAKE
        private set
    var version = Version.UNDEFINED
        private set
    var destination: InetSocketAddress? = null
        private set

    var address: InetSocketAddress = channel.remoteAddress() as InetSocketAddress
        private set

    var name: String? = null
        private set

    override fun channelActive(ctx: ChannelHandlerContext) {
        debug("has connected")
        super.channelActive(ctx)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        when (msg) {
            is HAProxyMessage -> {
                address = InetSocketAddress.createUnresolved(msg.sourceAddress(), msg.sourcePort())
                pipeline.remove(HAProxyMessageDecoder::class.java)
            }
            is PacketHandshake -> handle(msg)
            is PacketStatusRequest -> handle(msg)
            is PacketStatusPing -> handle(msg)
        }
        super.channelRead(ctx, msg)
    }

    private fun updateState(state: State) {
        encoder.let {
            it.version = this.version
            it.mappings = state.clientbound.value
        }
        decoder.let {
            it.version = this.version
            it.mappings = state.serverbound.value
        }
        this.state=state
    }

    private fun handle(packet: PacketHandshake) {
        this.version = packet.version
        this.destination = InetSocketAddress.createUnresolved(packet.host, packet.port)
        this.updateState(packet.nextState)
    }

    private fun handle(packet: PacketLoginRequest) {
        this.name=packet.name
        write(PacketLoginResponse(packet.name, UUIDUtil.generateOfflinePlayerUuid(packet.name)))
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handle(packet: PacketLoginAcknowledged) {
        updateState(State.CONFIGURATION)
        write(PacketRegistryData(config.world.toTag(version.toNbtVersion())))
        write(PacketFinishConfiguration(), true)
        updateState(State.PLAY)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handle(packet: PacketStatusRequest) {
        validate?.let {
            require(!it.recvStatusRequest) { "Cannot request status twice!" }
            it.recvStatusRequest=true
        }
        // TODO Send response
        write(PacketStatusResponse("{\"version\":{\"name\":\"§dBlessing\",\"protocol\":${version.protocolId} },\"players\":{\"max\":0,\"online\":0,\"sample\":[]},\"description\":{\"text\":\"§dBlessing <3\"}}"), true)
    }

    private fun handle(packet: PacketStatusPing) {
        validate?.let {
            require(it.recvStatusRequest && !it.recvStatusPing)
            { "Cannot send twice status ping or skipped status request!" }
            it.recvStatusPing=true
        }
        channel.writeAndFlush(packet).addListener(ChannelFutureListener.CLOSE)
    }

    fun flush(): Channel = channel.flush()

    @JvmOverloads
    fun write(packet: PacketToClient, flush: Boolean = false) {
        if (flush) channel.writeAndFlush(packet) else channel.write(packet)
    }

    @JvmOverloads
    fun write(byteBuf: ByteBuf, flush: Boolean = false) {
        if (flush) channel.writeAndFlush(byteBuf) else channel.write(byteBuf)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        config.debugLogger?.log(Level.WARNING, "$this - throws exception", cause)
        FallbackInitializer.exceptionHandler?.exceptionCaught(ctx, cause)
        channel.close()
    }

    fun debug(message: String) = config.debugLogger?.log(Level.INFO, "$this: $message") ?: Unit // void

    override fun toString() = "FallbackHandler[${version.name}|${address.address.hostAddress}|${name}]"
}