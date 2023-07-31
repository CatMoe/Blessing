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

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher
import catmoe.fallencrystal.moefilter.network.limbo.compat.FakeInitialHandler
import catmoe.fallencrystal.moefilter.network.limbo.compat.LimboCompat
import catmoe.fallencrystal.moefilter.network.limbo.netty.LimboDecoder
import catmoe.fallencrystal.moefilter.network.limbo.netty.LimboEncoder
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.PacketSnapshot
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.EnumPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.EnumPacket.*
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.PacketCache
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketKeepAlive
import catmoe.fallencrystal.moefilter.network.limbo.packet.handshake.Protocol
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.PacketEmptyChunk
import catmoe.fallencrystal.moefilter.network.limbo.util.LimboLocation
import catmoe.fallencrystal.moefilter.network.limbo.util.Version
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@Suppress("MemberVisibilityCanBePrivate")
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


    var location: LimboLocation? = null

   private fun getFakeProxyHandler(): LimboCompat { return FakeInitialHandler(ctx) }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        if (state == Protocol.PLAY) MoeLimbo.connections.remove(this)
        MessageUtil.logInfo("[MoeLimbo] Client disconnected.")
        super.channelInactive(ctx)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { ExceptionCatcher.handle(channel, cause) }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) { if (msg is LimboPacket) msg.handle(this) }

    fun fireLoginSuccess() {
        sendPacket(getCachedPacket(LOGIN_SUCCESS))
        state = Protocol.PLAY
        MoeLimbo.connections.add(this)
        updateVersion(version!!, state!!)
        sendPlayPackets()
        if (fakeHandler is FakeInitialHandler) fakeHandler.username=profile.username
    }

    fun updateVersion(version: Version, state: Protocol) {
        this.version=version
        encoder.switchVersion(version, state)
        decoder.switchVersion(version, state)
        if (fakeHandler is FakeInitialHandler) { fakeHandler.v = version }
    }

    @Suppress("SpellCheckingInspection")
    private fun sendPlayPackets() {
        writePacket(JOIN_GAME)
        // Weeee—— don't want player is flying, So don't send the packet insteadof apply flags.
        // writePacket(PLAYER_ABILITIES)

        writePacket(POS_AND_LOOK)
        writePacket(SPAWN_POSITION)
        writePacket(PLAYER_INFO)
        writePacket(PLUGIN_MESSAGE)
        keepAliveScheduler()

        // Empty chunk still is beta.
        val chunk = PacketEmptyChunk()
        (-1..1).forEach {x -> (-1..1).forEach { z -> chunk.x=x; chunk.z=z; writePacket(chunk) } }
    }

    private fun keepAliveScheduler() {
        Scheduler(MoeFilter.instance).repeatScheduler( 10, TimeUnit.SECONDS) {
            if (!MoeLimbo.connections.contains(this)) return@repeatScheduler
            val keepAlive = PacketKeepAlive()
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