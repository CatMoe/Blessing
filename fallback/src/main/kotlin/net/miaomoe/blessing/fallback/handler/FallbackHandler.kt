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
import net.kyori.adventure.text.Component
import net.miaomoe.blessing.fallback.cache.CachedMessage
import net.miaomoe.blessing.fallback.cache.ChunksCache
import net.miaomoe.blessing.fallback.cache.FallbackPacketsCache
import net.miaomoe.blessing.fallback.cache.PacketCacheGroup
import net.miaomoe.blessing.nbt.chat.MixedComponent
import net.miaomoe.blessing.protocol.message.Title
import net.miaomoe.blessing.protocol.message.TitleAction
import net.miaomoe.blessing.protocol.message.TitleTime
import net.miaomoe.blessing.protocol.packet.common.PacketDisconnect
import net.miaomoe.blessing.protocol.packet.common.PacketKeepAlive
import net.miaomoe.blessing.protocol.packet.common.PacketPluginMessage
import net.miaomoe.blessing.protocol.packet.configuration.PacketFinishConfiguration
import net.miaomoe.blessing.protocol.packet.handshake.PacketHandshake
import net.miaomoe.blessing.protocol.packet.login.PacketLoginAcknowledged
import net.miaomoe.blessing.protocol.packet.login.PacketLoginRequest
import net.miaomoe.blessing.protocol.packet.play.*
import net.miaomoe.blessing.protocol.packet.status.PacketStatusPing
import net.miaomoe.blessing.protocol.packet.status.PacketStatusRequest
import net.miaomoe.blessing.protocol.packet.status.PacketStatusResponse
import net.miaomoe.blessing.protocol.packet.type.PacketToEncode
import net.miaomoe.blessing.protocol.registry.State
import net.miaomoe.blessing.protocol.util.ByteMessage
import net.miaomoe.blessing.protocol.util.ComponentUtil.toComponent
import net.miaomoe.blessing.protocol.util.PlayerPosition
import net.miaomoe.blessing.protocol.version.Version
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import java.util.function.Supplier
import java.util.logging.Level

@Suppress("MemberVisibilityCanBePrivate")
class FallbackHandler(
    val initializer: FallbackInitializer,
    val channel: Channel
) : ChannelInboundHandlerAdapter() {

    val settings = initializer.settings

    var processLogic = settings.isProcessLogic

    var state = settings.defaultState
        private set

    val encoder = FallbackEncoder(state.clientbound.value, handler = this)
    val decoder = FallbackDecoder(state.serverbound.value, handler = this)

    val pipeline: ChannelPipeline = channel.pipeline()

    private val validate = if (settings.isValidate) ValidateHandler(this) else null

    var location: PlayerPosition? = null
    var version = Version.UNDEFINED
    var destination: InetSocketAddress? = null
    var address: InetSocketAddress = channel.remoteAddress() as InetSocketAddress

    var markDisconnect = false

    var name: String? = null
        private set
    var brand: String? = null
        private set

    override fun channelActive(ctx: ChannelHandlerContext) {
        debug { "has connected" }
        super.channelActive(ctx)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        debug { "has disconnected" }
        super.channelInactive(ctx)
        markDisconnect=true
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        if (!markDisconnect) {
            when (msg) {
                is HAProxyMessage -> {
                    address = InetSocketAddress.createUnresolved(msg.sourceAddress(), msg.sourcePort())
                    pipeline.remove(HAProxyMessageDecoder::class.java)
                }
                is PacketHandshake -> handle(msg)
                is PacketStatusRequest -> handle(msg)
                is PacketStatusPing -> handle(msg)
                is PacketLoginRequest -> handle(msg)
                is PacketLoginAcknowledged -> handle(msg)
                is PacketPositionLook -> handle(msg)
                is PacketPosition -> handle(msg)
                is PacketOnGround -> handle(msg)
                is PacketFinishConfiguration -> handle(msg)
                is PacketPluginMessage -> handle(msg)
            }
        }
        super.channelRead(ctx, msg)
    }

    fun updateState(state: State) {
        if (this.state == state) return
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
        val version = packet.version
        this.version = version
        this.destination = InetSocketAddress.createUnresolved(packet.host, packet.port)
        if (processLogic) {
            this.updateState(packet.nextState)
            if ((version == Version.UNDEFINED || !version.isSupported) && packet.nextState == State.LOGIN) {
                disconnect("<red>Unsupported client version.".toComponent())
                return
            }
        }
    }

    private fun handle(packet: PacketLoginRequest) {
        this.name=packet.name
        if (processLogic) {
            validate?.let {
                require(!it.recvLogin) { "Duplicated PacketLoginRequest!" }
                it.recvLogin=true
            }
            write(FallbackPacketsCache.LOGIN_RESPONSE, true)
            if (version.less(Version.V1_20_2)) {
                if (version.less(Version.V1_8)) {
                    updateState(State.PLAY)
                    channel.eventLoop().schedule(::spawn, 100, TimeUnit.MILLISECONDS)
                } else spawn()
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handle(packet: PacketLoginAcknowledged) {
        if (processLogic) {
            updateState(State.CONFIGURATION)
            write(FallbackPacketsCache.REGISTRY_DATA)
            write(FallbackPacketsCache.PLUGIN_MESSAGE)
            write(PacketFinishConfiguration(), true)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handle(packet: PacketFinishConfiguration) {
        if (processLogic) spawn()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handle(packet: PacketStatusRequest) {
        if (processLogic) {
            validate?.let {
                require(!it.recvStatusRequest) { "Cannot request status twice!" }
                it.recvStatusRequest=true
            }
            write(PacketStatusResponse(settings.motdHandler.handle(this).toJson()), true)
        }
    }

    private fun handle(packet: PacketStatusPing) {
        if (processLogic) {
            validate?.let {
                require(it.recvStatusRequest && !it.recvStatusPing)
                { "Cannot send twice status ping or skipped status request!" }
                it.recvStatusPing=true
            }
            channel.writeAndFlush(packet).addListener(ChannelFutureListener.CLOSE)
        }
    }

    private fun handle(packet: PacketPositionLook) {
        this.location = packet.position
    }

    private fun handle(packet: PacketPosition) {
        this.location = this.location?.copy(packet.position)
    }

    private fun handle(packet: PacketOnGround) {
        this.location = this.location?.copy(onGround = packet.onGround)
    }

    private fun handle(packet: PacketPluginMessage) {
        val channel = packet.channel
        if (
            (version.fromTo(Version.V1_8, Version.V1_12_2) && channel == "MC|Brand") ||
            (version.fromTo(Version.V1_13, Version.max) && channel == "minecraft:brand")
        ) {
            this.brand = ByteMessage.create().let {
                it.writeBytes(packet.data)
                it.readString()
            }
            validate?.let {
                require(!it.recvBrand) { "Cannot receive brand message twice!" }
                it.recvBrand = true
            }
        }
    }

    fun spawn() {
        updateState(State.PLAY)
        write(FallbackPacketsCache.JOIN_GAME)
        if (version.moreOrEqual(Version.V1_19_3)) write(FallbackPacketsCache.SPAWN_POSITION)
        if (version.less(Version.V1_20_2)) write(FallbackPacketsCache.PLUGIN_MESSAGE)
        write(FallbackPacketsCache.JOIN_POSITION)
        if (version.moreOrEqual(Version.V1_20_3)) write(FallbackPacketsCache.GAME_EVENT)
        val chunks = initializer.chunksCache ?: ChunksCache.surround(settings.joinPosition.position, 1)
        chunks.write(this)
        if (chunks != initializer.chunksCache) chunks.caches.clear()
        if (settings.isDisableFall) write(FallbackPacketsCache.PLAYER_ABILITIES)
        write(PacketKeepAlive(), true)
    }

    fun disconnect(reason: MixedComponent) {
        require(state != State.HANDSHAKE && state != State.STATUS)
        { "Only can send disconnect message when connection try to joining!" }
        markDisconnect=true
        val packet = PacketDisconnect(reason, this.state == State.LOGIN)
        if (version.less(Version.V1_8)) { // delay for 1.7
            channel.eventLoop().schedule({disconnect(packet)}, 150, TimeUnit.MILLISECONDS)
        } else
            disconnect(packet)
    }

    private fun disconnect(packet: PacketDisconnect) {
        channel.writeAndFlush(packet).addListener(ChannelFutureListener.CLOSE)
    }

    fun disconnect(reason: Component) = disconnect(MixedComponent(reason))

    @JvmOverloads
    fun sendMessage(component: MixedComponent, flush: Boolean = true) {
        require(state == State.PLAY) { "Only can send chat when state is State.PLAY!" }
        this.write(PacketServerChat(component), flush)
    }

    @JvmOverloads
    fun sendMessage(group: CachedMessage, flush: Boolean = true) {
        this.write(group.group, flush)
    }

    @JvmOverloads
    fun sendMessage(component: Component, flush: Boolean = true)
    = this.sendMessage(MixedComponent(component), flush)

    @JvmOverloads
    fun sendActionbar(component: MixedComponent, flush: Boolean = true) {
        require(state == State.PLAY) { "Only can send actionbar when state is State.PLAY!" }
        if (version.fromTo(Version.V1_11_1, Version.V1_16_4))
            write(PacketLegacyTitle(TitleAction.ACTION_BAR, component), flush)
        else
            write(PacketServerChat(component, PacketServerChat.Type.ACTION_BAR), flush)
    }

    @JvmOverloads
    fun sendActionbar(component: Component, flush: Boolean = true)
    = this.sendActionbar(MixedComponent(component), flush)

    @JvmOverloads
    fun writeTitle(title: Title, flush: Boolean = true) {
        writeTitle(TitleAction.TITLE, title.title)
        writeTitle(TitleAction.SUBTITLE, title.subTitle)
        writeTitle(TitleAction.TIMES, title.times, flush)
    }

    @JvmOverloads
    fun <T> writeTitle(action: TitleAction<T>, value: T, flush: Boolean = false) {
        require(state == State.PLAY) { "Only can send title when state is State.PLAY!" }
        require(!(action is TitleAction.NullTitleAction || action == TitleAction.ACTION_BAR))
        { "Wrong action! Use sendActionbar to send actionbar. Use resetTitle to reset the title." }
        if (version.less(Version.V1_17)) {
            write(PacketLegacyTitle(action, value as Any), flush)
        } else {
            write(
                when (action.id) {
                    0 -> PacketTitle(value as MixedComponent)
                    1 -> PacketSubTitle(value as MixedComponent)
                    3 -> PacketTitleTimes(value as TitleTime)
                    else -> throw IllegalArgumentException("Unsupported or unknown action id ${action.id}!")
                },
                flush
            )
        }
    }

    @JvmOverloads
    fun resetTitle(flush: Boolean = true) {
        require(state == State.PLAY) { "Only can reset title when state is State.PLAY!" }
        if (version.moreOrEqual(Version.V1_17))
            write(PacketTitleReset(), flush)
        else
            PacketLegacyTitle(TitleAction.RESET, flush)
    }

    @JvmOverloads
    fun clearTitle(flush: Boolean = true) {
        require(state == State.PLAY) { "Only can clear title when state is State.PLAY!" }
        writeTitle(TitleAction.TITLE, MixedComponent.EMPTY, false)
        writeTitle(TitleAction.SUBTITLE, MixedComponent.EMPTY, false)
        writeTitle(TitleAction.TIMES, TitleTime.zero, flush)
    }


    fun flush(): Channel = channel.flush()

    @JvmOverloads
    fun write(packet: PacketToEncode, flush: Boolean = false) {
        if (flush) channel.writeAndFlush(packet) else channel.write(packet)
    }

    @JvmOverloads
    fun write(byteBuf: ByteBuf, flush: Boolean = false) {
        if (flush) channel.writeAndFlush(byteBuf) else channel.write(byteBuf)
    }

    @JvmOverloads
    fun write(enum: FallbackPacketsCache, flush: Boolean = false) {
        if (settings.isUseCache) {
            initializer.cache[enum]?.let { this.write(it, flush) } ?: throw NullPointerException("Cached group for ${enum.name} is null!")
        } else { enum.packet.apply(settings, this.version)?.let { this.write(it, flush) } }
    }

    @JvmOverloads
    fun write(group: PacketCacheGroup, flush: Boolean = false) {
        group.getIfCached(version)?.let { write(it, flush) }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        settings.debugLogger?.log(Level.WARNING, "$this - throws exception", cause)
        settings.exceptionHandler?.exceptionCaught(ctx, cause) ?: channel.close() // Unit
    }

    @Deprecated(
        "Use supplier to print debug message. Avoid performance issues when don't need them.",
        replaceWith = ReplaceWith("debug"),
        level = DeprecationLevel.ERROR // Can cause huge performance issues. Stopping using this method now.
    )
    fun debug(message: String) = this.debug { message }

    fun debug(message: Supplier<String>) {
        settings.debugLogger?.log(Level.INFO, "$this: ${message.get()}")
    }

    override fun toString() = "FallbackHandler[State=${this.state.name}|${address.address.hostAddress}" + when (this.state) {
        State.HANDSHAKE -> ""
        State.STATUS, State.LOGIN -> "|${version.name}(${version.protocolId})|${destination?.let { "${it.hostString}:${it.port}" }}"
        State.PLAY, State.CONFIGURATION -> "|${version.name}(${version.protocolId})|$name"
    } + "]"
}