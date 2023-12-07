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

package catmoe.fallencrystal.moefilter.network.bungee.initializer

import catmoe.fallencrystal.moefilter.common.counter.ConnectionStatistics
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.common.firewall.Throttler
import catmoe.fallencrystal.moefilter.data.BlockType
import catmoe.fallencrystal.moefilter.network.bungee.handler.PacketAntibotHandler
import catmoe.fallencrystal.moefilter.network.bungee.handler.InboundHandler
import catmoe.fallencrystal.moefilter.network.bungee.handler.TimeoutHandler
import catmoe.fallencrystal.moefilter.network.bungee.initializer.geyser.GeyserInitializer
import catmoe.fallencrystal.moefilter.network.bungee.util.event.BungeeConnectedEvent
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import catmoe.fallencrystal.moefilter.network.common.decoder.VarIntFrameDecoder
import catmoe.fallencrystal.moefilter.network.common.decoder.VarIntLengthEncoder
import catmoe.fallencrystal.moefilter.network.common.haproxy.HAProxyManager
import catmoe.fallencrystal.moefilter.network.common.traffic.TrafficManager
import catmoe.fallencrystal.moefilter.network.common.traffic.TrafficMonitor
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboDecoder
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboEncoder
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.util.BungeeSwitcher
import io.netty.channel.*
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.config.ListenerInfo
import net.md_5.bungee.connection.InitialHandler
import net.md_5.bungee.netty.PipelineUtils.*
import net.md_5.bungee.protocol.*
import java.net.InetSocketAddress

@Suppress("HasPlatformType", "MemberVisibilityCanBePrivate")
abstract class AbstractInitializer : ChannelInitializer<Channel>(), IPipeline {
    val bungee = BungeeCord.getInstance()
    val throttler = bungee.connectionThrottle
    val legacyKicker = KickStringWriter()
    val protocol = 0

    @Throws(Exception::class)
    override fun initChannel(channel: Channel) {
        if (GeyserInitializer.isGeyser(channel)) { GeyserInitializer().handle(channel, protocol) }
    }

    @Throws(Exception::class)
    override fun handlerAdded(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        val pipeline = channel.pipeline()
        val listener = channel.attr(LISTENER).get()
        if (listener.isProxyProtocol) HAProxyManager.handle(ctx)
        val remoteAddress = if (channel.remoteAddress() == null) channel.parent().localAddress() else channel.remoteAddress()
        val inetAddress = (remoteAddress as InetSocketAddress).address
        ConnectionStatistics.increase(inetAddress)
        if (Firewall.isFirewalled(inetAddress)) { channel.close(); ConnectionStatistics.countBlocked(BlockType.FIREWALL); return }
        if (Throttler.increase(inetAddress)) { channel.close(); ConnectionStatistics.countBlocked(BlockType.FIREWALL); return }
        if (throttler != null && throttler.throttle(remoteAddress)) { channel.close(); ConnectionStatistics.countBlocked(BlockType.FIREWALL); return }
        pipeline.addFirst(TrafficMonitor.NAME, TrafficMonitor())
        if (BungeeSwitcher.connectToBungee(inetAddress)) connectToBungee(ctx, pipeline, channel, listener)
        else connectToLimbo(ctx, pipeline, channel)
        BungeeConnectedEvent(channel, listener).callEvent()
    }

    open fun connectToLimbo(ctx: ChannelHandlerContext, pipeline: ChannelPipeline, channel: Channel) {
        val decoder = LimboDecoder(null)
        val encoder = LimboEncoder(null)
        val handler = LimboHandler(encoder, decoder, channel, ctx)
        decoder.handler=handler
        encoder.handler=handler
        pipeline.addFirst(TIMEOUT_HANDLER, TimeoutHandler(MoeChannelHandler.dynamicTimeout))
        TrafficManager.addLimiter(pipeline, TrafficManager.limbo, false)
        pipeline.addLast(FRAME_DECODER, VarIntFrameDecoder())
        pipeline.addLast(FRAME_PREPENDER, VarIntLengthEncoder())
        pipeline.addLast(PACKET_DECODER, decoder)
        pipeline.addLast(PACKET_ENCODER, encoder)
        pipeline.addLast(BOSS_HANDLER, handler)
    }

    open fun connectToBungee(ctx: ChannelHandlerContext, pipeline: ChannelPipeline, channel: Channel, listener: ListenerInfo) {
        MoeChannelHandler.register(pipeline)
        BASE.initChannel(channel)

        // MoeFilter: TrafficLimiter
        TrafficManager.addLimiter(pipeline, TrafficManager.proxy, true)

        // MoeFilter has VarIntFrameDecoder, TimeoutHandler and InboundHandler itself.
        pipeline.replace(FRAME_DECODER, FRAME_DECODER, VarIntFrameDecoder())
        // like https://github.com/PaperMC/Waterfall/commit/6702e0f69b2fa32c1046d277ade2107e22ba9134
        pipeline.replace(TIMEOUT_HANDLER, TIMEOUT_HANDLER, TimeoutHandler(MoeChannelHandler.dynamicTimeout))
        pipeline.replace(BOSS_HANDLER, BOSS_HANDLER, InboundHandler())

        // Add PacketListener
        if (MoeChannelHandler.injectPacketListener)
            pipeline.addBefore(BOSS_HANDLER, IPipeline.PACKET_INTERCEPTOR, PacketAntibotHandler(ctx))

        // Init default BungeeCord pipeline
        pipeline.addBefore(FRAME_DECODER, LEGACY_DECODER, LegacyDecoder())
        pipeline.addAfter(FRAME_DECODER, PACKET_DECODER, MinecraftDecoder(Protocol.HANDSHAKE, true, protocol))
        pipeline.addAfter(FRAME_PREPENDER, PACKET_ENCODER, MinecraftEncoder(Protocol.HANDSHAKE, true, protocol))
        pipeline.addBefore(FRAME_PREPENDER, LEGACY_KICKER, legacyKicker)

        // MoeFilter -- TND default should be true.
        channel.config().setOption(ChannelOption.TCP_NODELAY, true)

        // MoeFilter's InitialHandler
        pipeline[InboundHandler::class.java].setHandler(InitialHandler(bungee, listener))

        if (listener.isProxyProtocol) pipeline.addFirst(HAProxyMessageDecoder())
        if (MoeChannelHandler.callInitEvent) BungeeConnectedEvent(channel, listener).callEvent()
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) { /* Ignored */ }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val address = (ctx.channel().remoteAddress() as InetSocketAddress).address
        if (Firewall.isFirewalled(address)) { super.channelInactive(ctx); return }
        if (MoeChannelHandler.sentHandshake.getIfPresent(ctx.channel()) != true) { Firewall.addAddressTemp(address) }
        super.channelInactive(ctx)
    }


    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) { ExceptionCatcher.handle(ctx!!.channel(), cause!!) }
}