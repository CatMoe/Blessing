/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package catmoe.fallencrystal.moefilter.network.limbo.handler

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import catmoe.fallencrystal.moefilter.network.limbo.compat.FakeInitialHandler
import catmoe.fallencrystal.moefilter.network.limbo.compat.LimboCompat
import catmoe.fallencrystal.moefilter.network.limbo.listener.LimboListener
import catmoe.fallencrystal.moefilter.network.limbo.netty.LimboDecoder
import catmoe.fallencrystal.moefilter.network.limbo.netty.LimboEncoder
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.EnumPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.EnumPacket.*
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.PacketCache
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.PacketSnapshot
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.Disconnect
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketKeepAlive
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketPluginMessage
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.moefilter.network.limbo.util.LimboLocation
import catmoe.fallencrystal.translation.utils.version.Version
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

@Suppress("MemberVisibilityCanBePrivate", "RedundantNullableReturnType")
class LimboHandler(
    private val encoder: LimboEncoder,
    private val decoder: LimboDecoder,
    val channel: Channel,
    val ctx: ChannelHandlerContext
) : ChannelInboundHandlerAdapter() {
    val address: SocketAddress = channel.remoteAddress()
    var state: Protocol? = null
    var version: Version? = null
    var host: InetSocketAddress? = null
    var profile: VirtualConnection = VirtualConnection()
    val fakeHandler: LimboCompat? = getFakeProxyHandler()

    val disconnected = AtomicBoolean(false)
    var brand = ""


    var location: LimboLocation? = null

   private fun getFakeProxyHandler(): LimboCompat { return FakeInitialHandler(ctx) }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        if (!disconnected.get()) fireDisconnect()
        super.channelInactive(ctx)
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        if (!disconnected.get()) fireDisconnect()
        super.handlerRemoved(ctx)
    }

    private fun fireDisconnect() {
        MoeLimbo.connections.remove(this)
        LimboListener.handleReceived(Disconnect(), this)
        MoeLimbo.debug("Client disconnected")
        disconnected.set(true)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { ExceptionCatcher.handle(channel, cause) }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) { if (msg is LimboPacket) msg.handle(this) }

    fun fireLoginSuccess() {
        sendPacket(getCachedPacket(LOGIN_SUCCESS))
        state = Protocol.PLAY
        MoeLimbo.connections.add(this)
        updateVersion(version!!, state!!)
        if (fakeHandler is FakeInitialHandler) fakeHandler.username=profile.username
        sendPlayPackets()
    }

    fun updateVersion(version: Version, state: Protocol) {
        this.version=version
        encoder.switchVersion(version, state)
        decoder.switchVersion(version, state)
        if (fakeHandler is FakeInitialHandler) { fakeHandler.v = version }
    }

    val keepAlive = PacketKeepAlive()

    @Suppress("SpellCheckingInspection")
    private fun sendPlayPackets() {
        //writePacket(JOIN_GAME)
        writePacket(JOIN_GAME)
        // Weeee—— don't want player is flying, So don't send the packet insteadof apply flags.
        // writePacket(PLAYER_ABILITIES)

        writePacket(POS_AND_LOOK)
        writePacket(SPAWN_POSITION)
        writePacket(PLAYER_INFO)

        if (version!!.moreOrEqual(Version.V1_8)) {
            val brand = PacketPluginMessage()
            brand.channel=if (version!!.moreOrEqual(Version.V1_13)) "minecraft:brand" else "MC|Brand"
            brand.message="MoeFilter <- MoeLimbo"
            writePacket(brand)
        }

        keepAliveScheduler()
        keepAlive.id = abs(ThreadLocalRandom.current().nextInt()).toLong()
        sendPacket(keepAlive)

        // Empty chunk still is beta.

        (-1..1).forEach { x -> (-1..1).forEach { z -> writePacket(EnumPacket.valueOf("CHUNK_${x+1}_${z+1}")) }}


        // sendCaptcha()
    }

    private fun keepAliveScheduler() {
        Scheduler(MoeFilterBungee.instance).repeatScheduler( LocalConfig.getLimbo().getLong("keep-alive.delay"), TimeUnit.SECONDS) {
            if (disconnected.get()) return@repeatScheduler
            keepAlive.id = abs(ThreadLocalRandom.current().nextInt()).toLong()
            sendPacket(keepAlive)
        }
    }

    private fun getCachedPacket(enumPacket: EnumPacket): PacketSnapshot? {
        return PacketCache.packetCache.getIfPresent(enumPacket)
    }

    private fun writePacket(packet: EnumPacket) { writePacket(getCachedPacket(packet)) }

    fun writePacket(packet: Any?) { if (channel.isActive && packet != null) channel.write(packet, channel.voidPromise()) }

    fun sendPacket(packet: Any?) { if (channel.isActive && packet != null) channel.writeAndFlush(packet, channel.voidPromise()) }

}