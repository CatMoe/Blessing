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

package catmoe.fallencrystal.moefilter.network.limbo.handler

import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import catmoe.fallencrystal.moefilter.network.limbo.LimboLocation
import catmoe.fallencrystal.moefilter.network.limbo.block.BlockPosition
import catmoe.fallencrystal.moefilter.network.limbo.compat.FakeInitialHandler
import catmoe.fallencrystal.moefilter.network.limbo.compat.LimboCompat
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo.chunkLength
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo.chunkSent
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo.chunkStart
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo.connections
import catmoe.fallencrystal.moefilter.network.limbo.listener.LimboListener
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.EnumPacket
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.EnumPacket.*
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.PacketCache
import catmoe.fallencrystal.moefilter.network.limbo.packet.cache.PacketSnapshot
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.Disconnect
import catmoe.fallencrystal.moefilter.network.limbo.packet.common.PacketKeepAlive
import catmoe.fallencrystal.moefilter.network.limbo.packet.protocol.Protocol
import catmoe.fallencrystal.moefilter.network.limbo.packet.s2c.PacketBlocksSectionUpdate
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.version.Version
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import net.md_5.bungee.api.scheduler.ScheduledTask
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
    var profile: LimboProfile = LimboProfile()
    val fakeHandler: LimboCompat? = getFakeProxyHandler()
    private val scheduler = Scheduler.getDefault()

    val disconnected = AtomicBoolean(false)
    var brand = ""


    var location: LimboLocation? = null

   private fun getFakeProxyHandler(): LimboCompat { return FakeInitialHandler(ctx) }

    override fun channelActive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        profile.channel=channel
        profile.address=channel.remoteAddress()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        if (!disconnected.get()) fireDisconnect()
        super.channelInactive(ctx)
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        if (!disconnected.get()) fireDisconnect()
        super.handlerRemoved(ctx)
    }

    private fun fireDisconnect() {
        connections.remove(this)
        LimboListener.handleReceived(Disconnect(), this)
        MoeLimbo.debug(this,"Client disconnected")
        disconnected.set(true)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { ExceptionCatcher.handle(channel, cause) }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) { if (msg is LimboPacket) msg.handle(this) }

    fun updateVersion(version: Version, state: Protocol) {
        this.state=state
        this.version=version
        encoder.switchVersion(version, state)
        decoder.switchVersion(version, state)
        if (fakeHandler is FakeInitialHandler) { fakeHandler.v = version }
    }

    @Suppress("SpellCheckingInspection")
    fun sendPlayPackets() {
        val version = this.version!!
        //writePacket(JOIN_GAME)
        writePacket(JOIN_GAME)
        // Weeee—— don't want player is flying, So don't send the packet insteadof apply flags.
        // writePacket(PLAYER_ABILITIES)

        writePacket(POS_AND_LOOK)
        writePacket(SPAWN_POSITION)
        writePacket(PLAYER_INFO)
        //writePacket(UPDATE_TIME)

        if (version.moreOrEqual(Version.V1_8) && version.less(Version.V1_20_2))
            // About ignore 1.20.2, See PacketLoginAcknowledged.handle() method.
            writePacket(if (version.moreOrEqual(Version.V1_13)) PLUGIN_MESSAGE else PLUGIN_MESSAGE_LEGACY)

        keepAliveScheduler()
        sendPacket(PacketKeepAlive(abs(ThreadLocalRandom.current().nextInt()).toLong()))

        /*
        *1: 不确定我们应该是否穷举方法, 因为对于valueOf和IntRange的forEach成本都似乎有些高昂.
        *2: 未解之谜: 为什么在部分情况下, 区块数据包如果比KeepAlive先发送 当服务器再发送KeepAlive时,
        *   客户端**不一定**会回应KeepAlive. 但客户端除了不回应心跳包之外,
        *   客户端设置, PluginMessage和移动数据包将向往常一样发送. 但无论如何发送心跳包客户端都不会回应
         */
        if (chunkSent) (chunkStart..chunkLength).forEach { x -> (chunkStart..chunkLength).forEach { z -> writePacket(EnumPacket.valueOf("CHUNK_${x+1}_${z+1}")) }}
        if (MoeLimbo.platformSummon) sendTestPlatform(MoeLimbo.platformHeight)
        channel.flush()
    }

    @Suppress("unused")
    private fun sendTestPlatform(y: Int) {
        val size = 16
        val offset = 7.5.toInt() shr 4
        val list = mutableListOf<BlockPosition>()
        for (x in 0 until size) {
            for (z in 0 until size) list.add(BlockPosition(MoeLimbo.platformBlock, x, y, z))
        }
        writePacket(PacketBlocksSectionUpdate(list, offset, offset))
    }

    private fun keepAliveScheduler() {
        var task: ScheduledTask? = null
        val delay = LocalConfig.getLimbo().getLong("keep-alive.delay")
        task = scheduler.repeatScheduler(delay, delay, TimeUnit.SECONDS) {
            if (disconnected.get()) task?.cancel()
            sendPacket(PacketKeepAlive(abs(ThreadLocalRandom.current().nextInt()).toLong()))
        }
    }

    fun getCachedPacket(enumPacket: EnumPacket): PacketSnapshot? {
        return PacketCache.packetCache.getIfPresent(enumPacket)
    }

    private fun writePacket(packet: EnumPacket) { writePacket(getCachedPacket(packet)) }

    fun writePacket(packet: Any?) { if (channel.isActive && packet != null) channel.write(packet, channel.voidPromise()) }

    fun sendPacket(packet: Any?) { if (channel.isActive && packet != null) channel.writeAndFlush(packet, channel.voidPromise()) }

}