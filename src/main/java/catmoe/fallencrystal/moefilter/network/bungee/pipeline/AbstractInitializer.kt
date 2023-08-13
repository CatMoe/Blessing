package catmoe.fallencrystal.moefilter.network.bungee.pipeline

import catmoe.fallencrystal.moefilter.common.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.counter.type.BlockType
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.common.firewall.Throttler
import catmoe.fallencrystal.moefilter.network.bungee.handler.InboundHandler
import catmoe.fallencrystal.moefilter.network.bungee.handler.MoeInitialHandler
import catmoe.fallencrystal.moefilter.network.bungee.handler.TimeoutHandler
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.geyser.GeyserInitializer
import catmoe.fallencrystal.moefilter.network.bungee.util.event.EventCallMode
import catmoe.fallencrystal.moefilter.network.bungee.util.event.EventCaller
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import catmoe.fallencrystal.moefilter.network.common.varint.VarIntFrameDecoder
import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.network.limbo.netty.LimboDecoder
import catmoe.fallencrystal.moefilter.network.limbo.netty.LimboEncoder
import catmoe.fallencrystal.moefilter.network.limbo.netty.VarIntLengthEncoder
import catmoe.fallencrystal.moefilter.network.limbo.util.BungeeSwitcher
import io.netty.channel.*
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.config.ListenerInfo
import net.md_5.bungee.connection.InitialHandler
import net.md_5.bungee.netty.PipelineUtils
import net.md_5.bungee.protocol.*
import java.net.InetSocketAddress

@Suppress("HasPlatformType", "MemberVisibilityCanBePrivate")
abstract class AbstractInitializer : ChannelInitializer<Channel>(), IPipeline {
    val bungee = BungeeCord.getInstance()
    val throttler = bungee.connectionThrottle
    val legacyKicker = KickStringWriter()
    val protocol = 0
    val hasJPremium = bungee.pluginManager.getPlugin("JPremium") != null

    @Throws(Exception::class)
    override fun initChannel(channel: Channel) {
        val parent = channel.parent()
        val isGeyser = parent != null && parent.javaClass.canonicalName.startsWith("org.geysermc.geyser")
        if (isGeyser) { GeyserInitializer().handle(channel, protocol) }
    }

    @Throws(Exception::class)
    override fun handlerAdded(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        val remoteAddress = if (channel.remoteAddress() == null) channel.parent().localAddress() else channel.remoteAddress()
        val inetAddress = (remoteAddress as InetSocketAddress).address
        val pipeline = channel.pipeline()
        val listener = channel.attr(PipelineUtils.LISTENER).get()
        val eventCaller = EventCaller(ctx, listener)

        ConnectionCounter.increase(inetAddress)
        eventCaller.call(EventCallMode.AFTER_INIT)
        if (Firewall.isFirewalled(inetAddress)) { channel.close(); ConnectionCounter.countBlocked(BlockType.FIREWALL); return }
        eventCaller.call(EventCallMode.NON_FIREWALL)
        if (Throttler.increase(inetAddress)) { channel.close(); ConnectionCounter.countBlocked(BlockType.FIREWALL); return }
        if (throttler != null && throttler.throttle(remoteAddress)) { channel.close(); ConnectionCounter.countBlocked(BlockType.FIREWALL); return }
        eventCaller.call(EventCallMode.READY_DECODING)
        if (!channel.isActive) { return }
        if (BungeeSwitcher.connectToBungee(inetAddress)) connectToBungee(ctx, pipeline, channel, eventCaller, listener)
        else connectToLimbo(ctx, pipeline, channel)
    }

    open fun connectToLimbo(ctx: ChannelHandlerContext, pipeline: ChannelPipeline, channel: Channel) {
        val decoder = LimboDecoder(null)
        val encoder = LimboEncoder(null)
        val handler = LimboHandler(encoder, decoder, channel, ctx)
        decoder.handler=handler
        encoder.handler=handler
        pipeline.addLast(PipelineUtils.TIMEOUT_HANDLER, TimeoutHandler(MoeChannelHandler.dynamicTimeout))
        pipeline.addLast(PipelineUtils.FRAME_DECODER, VarIntFrameDecoder())
        pipeline.addLast(PipelineUtils.FRAME_PREPENDER, VarIntLengthEncoder())
        pipeline.addLast(PipelineUtils.PACKET_DECODER, decoder)
        pipeline.addLast(PipelineUtils.PACKET_ENCODER, encoder)
        pipeline.addLast(PipelineUtils.BOSS_HANDLER, handler)
    }

    open fun connectToBungee(ctx: ChannelHandlerContext, pipeline: ChannelPipeline, channel: Channel, eventCaller: EventCaller, listener: ListenerInfo) {
        MoeChannelHandler.register(pipeline)
        PipelineUtils.BASE.initChannel(channel)

        // MoeFilter has VarIntFrameDecoder, TimeoutHandler and InboundHandler itself.
        pipeline.replace(PipelineUtils.FRAME_DECODER, PipelineUtils.FRAME_DECODER, VarIntFrameDecoder())
        // like https://github.com/PaperMC/Waterfall/commit/6702e0f69b2fa32c1046d277ade2107e22ba9134
        pipeline.replace(PipelineUtils.TIMEOUT_HANDLER, PipelineUtils.TIMEOUT_HANDLER, TimeoutHandler(MoeChannelHandler.dynamicTimeout))
        pipeline.replace(PipelineUtils.BOSS_HANDLER, PipelineUtils.BOSS_HANDLER, InboundHandler())

        // Init default BungeeCord pipeline
        pipeline.addBefore(PipelineUtils.FRAME_DECODER, PipelineUtils.LEGACY_DECODER, LegacyDecoder())
        pipeline.addAfter(PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, MinecraftDecoder(Protocol.HANDSHAKE, true, protocol))
        pipeline.addAfter(PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, MinecraftEncoder(Protocol.HANDSHAKE, true, protocol))
        pipeline.addBefore(PipelineUtils.FRAME_PREPENDER, PipelineUtils.LEGACY_KICKER, legacyKicker)

        // MoeFilter -- TND default should be true.
        channel.config().setOption(ChannelOption.TCP_NODELAY, true)

        // MoeFilter's InitialHandler
        pipeline.get(InboundHandler::class.java).setHandler(
            if (MoeLimbo.useOriginalHandler) InitialHandler(bungee, listener) else MoeInitialHandler(ctx, listener)
        )

        if (listener.isProxyProtocol) pipeline.addFirst(HAProxyMessageDecoder())

        eventCaller.call(EventCallMode.AFTER_DECODER)
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