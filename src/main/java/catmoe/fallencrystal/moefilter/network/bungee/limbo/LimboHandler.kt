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

package catmoe.fallencrystal.moefilter.network.bungee.limbo

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.network.bungee.limbo.netty.LimboDecoder
import catmoe.fallencrystal.moefilter.network.bungee.limbo.netty.LimboEncoder
import catmoe.fallencrystal.moefilter.network.bungee.limbo.netty.PacketHandler
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.PacketSnapshot
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.cache.EnumPacket
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.cache.EnumPacket.*
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.cache.PacketCache
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packet.common.PacketKeepAlive
import catmoe.fallencrystal.moefilter.network.bungee.limbo.util.LimboLocation
import catmoe.fallencrystal.moefilter.network.bungee.limbo.util.Version
import catmoe.fallencrystal.moefilter.network.bungee.limbo.util.handshake.Protocol
import catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

class LimboHandler(
    val encoder: LimboEncoder,
    val decoder: LimboDecoder,
    val channel: Channel
) : ChannelInboundHandlerAdapter() {
    val address: SocketAddress = channel.remoteAddress()
    var state: Protocol? = null
    var version: Version? = null
    var host: InetSocketAddress? = null
    var profile: VirtualConnection = VirtualConnection()

    val packetHandler = PacketHandler()

    var location: LimboLocation? = null

    override fun channelInactive(ctx: ChannelHandlerContext) {
        if (state == Protocol.PLAY) MoeLimbo.connections.remove(this)
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
    }

    fun updateVersion(version: Version, state: Protocol) {
        this.version=version
        encoder.switchVersion(version, state)
        decoder.switchVersion(version, state)
    }

    private fun sendPlayPackets() {
        val version = this.version!!
        writePacket(JOIN_GAME)
        writePacket(PLAYER_ABILITIES)

        if (version.less(Version.V1_9)) writePacket(POS_AND_LOOK_LEGACY) else writePacket(POS_AND_LOOK)
        if (version.moreOrEqual(Version.V1_19_3)) writePacket(SPAWN_POSITION)
        writePacket(PLAYER_INFO)
        writePacket(PLUGIN_MESSAGE)

        keepAliveScheduler()
    }

    private fun keepAliveScheduler() {
        Scheduler(MoeFilter.instance).repeatScheduler(5, TimeUnit.SECONDS) {
            if (!MoeLimbo.connections.contains(this)) return@repeatScheduler
            val keepAlive = PacketKeepAlive()
            keepAlive.id= ThreadLocalRandom.current().nextInt()
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