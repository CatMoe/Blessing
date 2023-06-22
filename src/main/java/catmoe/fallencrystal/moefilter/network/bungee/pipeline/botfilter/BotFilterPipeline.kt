package catmoe.fallencrystal.moefilter.network.bungee.pipeline.botfilter

import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.common.utils.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.listener.firewall.FirewallCache
import catmoe.fallencrystal.moefilter.listener.firewall.Throttler
import catmoe.fallencrystal.moefilter.network.bungee.handler.InboundHandler
import catmoe.fallencrystal.moefilter.network.bungee.handler.PlayerHandler
import catmoe.fallencrystal.moefilter.network.bungee.handler.TimeoutHandler
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.IPipeline
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.MoeChannelHandler
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.geyser.GeyserPipeline
import catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher
import catmoe.fallencrystal.moefilter.network.bungee.util.event.EventCallMode
import catmoe.fallencrystal.moefilter.network.bungee.util.event.EventCaller
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder
import lombok.RequiredArgsConstructor
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.netty.PipelineUtils
import net.md_5.bungee.protocol.*
import java.net.InetSocketAddress

@RequiredArgsConstructor
class BotFilterPipeline : ChannelInitializer<Channel>(), IPipeline {
    private val bungee = BungeeCord.getInstance()
    private val throttler = bungee.connectionThrottle
    private val protocol = 0
    private val legacyKicker = KickStringWriter()

    @Throws(Exception::class)
    override fun initChannel(channel: Channel) {
        val parent = channel.parent()
        val isGeyser = parent != null && parent.javaClass.canonicalName.startsWith("org.geysermc.geyser")
        if (isGeyser) { GeyserPipeline().handle(channel, protocol) }
    }

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        try {
            val channel = ctx.channel()
            val remoteAddress = if (channel.remoteAddress() == null) channel.parent().localAddress() else channel.remoteAddress()
            val inetAddress = (remoteAddress as InetSocketAddress).address
            val pipeline = channel.pipeline()
            val listener = channel.attr(PipelineUtils.LISTENER).get()
            val eventCaller = EventCaller(channel, listener)

            ConnectionCounter.increase(inetAddress)
            eventCaller.call(EventCallMode.AFTER_INIT)
            if (FirewallCache.isFirewalled(inetAddress)) { channel.close(); return }
            eventCaller.call(EventCallMode.NON_FIREWALL)
            if (Throttler.increase(inetAddress)) { channel.close(); return }
            if (throttler != null && throttler.throttle(remoteAddress)) { channel.close(); return }
            eventCaller.call(EventCallMode.READY_DECODING)

            if (ProxyCache.isProxy(inetAddress)) { FirewallCache.addAddress(inetAddress, true); channel.close(); return }
            MoeChannelHandler.register(pipeline)
            PipelineUtils.BASE.initChannel(channel)

            // VarIntFameDecoder exploit is safe for BotFilter.
            // pipeline.replace(PipelineUtils.FRAME_DECODER, PipelineUtils.FRAME_DECODER, VarIntFrameDecoder())
            pipeline.replace(PipelineUtils.TIMEOUT_HANDLER, PipelineUtils.TIMEOUT_HANDLER, TimeoutHandler(BungeeCord.getInstance().getConfig().timeout.toLong()))
            pipeline.replace(PipelineUtils.BOSS_HANDLER, PipelineUtils.BOSS_HANDLER, InboundHandler())

            pipeline.addBefore(PipelineUtils.FRAME_DECODER, PipelineUtils.LEGACY_DECODER, LegacyDecoder())
            pipeline.addAfter(PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, MinecraftDecoder(Protocol.HANDSHAKE, true, protocol))
            pipeline.addAfter(PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, MinecraftEncoder(Protocol.HANDSHAKE, true, protocol))
            pipeline.addBefore(PipelineUtils.FRAME_PREPENDER, PipelineUtils.LEGACY_KICKER, legacyKicker)

            // TCP NO DELAY is always enabled on BotFilter/Waterfall

            // MoeFilter's InitialHandler
            pipeline.get(InboundHandler::class.java).setHandler(PlayerHandler(ctx, listener))

            if (listener.isProxyProtocol) pipeline.addFirst(HAProxyMessageDecoder())

            eventCaller.call(EventCallMode.AFTER_DECODER)
        } finally { if (!ctx.isRemoved) { ctx.pipeline().remove(this) } }
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) { /* Ignored */ }

    @Deprecated("Deprecated in Java", ReplaceWith("ExceptionCatcher.handle(ctx.channel(), cause)", "catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher"))
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { ExceptionCatcher.handle(ctx.channel(), cause) }
}