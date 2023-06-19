package catmoe.fallencrystal.moefilter.network.bungee.pipeline

import catmoe.fallencrystal.moefilter.common.utils.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.listener.firewall.Throttler
import catmoe.fallencrystal.moefilter.network.bungee.decoder.VarIntFrameDecoder
import catmoe.fallencrystal.moefilter.network.bungee.handler.InboundHandler
import catmoe.fallencrystal.moefilter.network.bungee.handler.PlayerHandler
import catmoe.fallencrystal.moefilter.network.bungee.handler.TimeoutHandler
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.geyser.GeyserPipeline
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import lombok.RequiredArgsConstructor
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.netty.PipelineUtils
import net.md_5.bungee.protocol.*
import java.net.InetSocketAddress

@RequiredArgsConstructor
class BungeePipeline : ChannelInitializer<Channel>(), IPipeline {
    private val throttler = BungeeCord.getInstance().connectionThrottle
    private val lk = KickStringWriter()
    private val protocol = 0

    init { logInfo("Injected.") }

    @Throws(Exception::class)
    override fun initChannel(channel: Channel) {
        logInfo("initChannel triggered. $channel")
        val parent = channel.parent()
        val isGeyser = parent != null && parent.javaClass.canonicalName.startsWith("org.geysermc.geyser")
        if (isGeyser) { GeyserPipeline().handle(channel, protocol) }
    }

    @Throws(Exception::class)
    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        MessageUtil.logInfo("handlerRemoved triggered. $ctx")
        try {
            val channel = ctx.channel()
            val remoteAddress = if (channel.remoteAddress() == null) channel.parent().localAddress() else channel.remoteAddress()
            val inetAddress = (remoteAddress as InetSocketAddress).address
            ConnectionCounter.increase(inetAddress)
            if (Throttler.increase(inetAddress)) { ctx.close(); return }
            val pipeline = channel.pipeline()
            val listener = channel.attr(PipelineUtils.LISTENER).get()
            PipelineUtils.BASE.initChannel(channel)

            // MoeFilter有自己的VarIntFrameDecoder TimeoutHandler和InboundHandler.
            pipeline.replace(PipelineUtils.FRAME_DECODER, PipelineUtils.FRAME_DECODER, VarIntFrameDecoder())
            pipeline.replace(PipelineUtils.TIMEOUT_HANDLER, PipelineUtils.TIMEOUT_HANDLER, TimeoutHandler(BungeeCord.getInstance().getConfig().timeout.toLong()))
            pipeline.replace(PipelineUtils.BOSS_HANDLER, PipelineUtils.BOSS_HANDLER, InboundHandler())

            // 默认BungeeCord监听管道初始化
            pipeline.addBefore(PipelineUtils.FRAME_DECODER, PipelineUtils.LEGACY_DECODER, LegacyDecoder())
            pipeline.addAfter(PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, MinecraftDecoder(Protocol.HANDSHAKE, true, protocol))
            pipeline.addAfter(PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, MinecraftEncoder(Protocol.HANDSHAKE, true, protocol))
            pipeline.addBefore(PipelineUtils.FRAME_PREPENDER, PipelineUtils.LEGACY_KICKER, lk)

            // 仍然是MoeFilter的
            pipeline.get(InboundHandler::class.java).setHandler(PlayerHandler(ctx, listener, throttler))
        } finally { if (!ctx.isRemoved) { ctx.pipeline().remove(this) } }
    }

    private fun logInfo(text: String) { MessageUtil.logInfo("[MoeFilter] [Pipeline] [Debug] BungeePipeline: $text") }
}
