package catmoe.fallencrystal.moefilter.network.pipeline.interceptor

import catmoe.fallencrystal.moefilter.network.decoder.VarIntFrameDecoder
import catmoe.fallencrystal.moefilter.network.handler.InboundHandler
import catmoe.fallencrystal.moefilter.network.handler.TimeoutHandler
import catmoe.fallencrystal.moefilter.network.pipeline.IPipeline
import catmoe.fallencrystal.moefilter.network.util.ChannelExceptionCatcher.handle
import catmoe.fallencrystal.moefilter.util.message.MessageUtil.logWarn
import com.github.benmanes.caffeine.cache.Caffeine
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import lombok.RequiredArgsConstructor
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.netty.PipelineUtils
import net.md_5.bungee.protocol.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.TimeUnit

@RequiredArgsConstructor
class BungeeInterceptor : ChannelInitializer<Channel>(), IPipeline {
    private val throttle = BungeeCord.getInstance().connectionThrottle
    private val legacyKick = KickStringWriter()
    private val perIpCount =
        Caffeine.newBuilder().expireAfterWrite(1L, TimeUnit.SECONDS).initialCapacity(1).build<InetAddress, Byte>()
    private val protocol = 0
    @Throws(Exception::class)
    override fun initChannel(channel: Channel) {
        val parent = channel.parent()
        val isGeyser = parent != null && parent.javaClass.canonicalName.startsWith("org.geysermc.geyser")
        if (isGeyser) { GeyserInterceptor().handle(channel, protocol); }
    }

    @Throws(Exception::class)
    override fun handlerRemoved(ctx: ChannelHandlerContext) { /* ? */
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "handle(ctx.channel(), cause)",
        "catmoe.fallencrystal.moefilter.network.util.ChannelExceptionCatcher.handle"))
    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        handle(ctx.channel(), cause)
    }

    @Throws(Exception::class)
    override fun handlerAdded(ctx: ChannelHandlerContext) {
        try {
            // TODO Counter ++
            val ch = ctx.channel()
            val address = if (ch.remoteAddress() == null) ch.parent().localAddress() else ch.remoteAddress()
            val inetAddress = (address as InetSocketAddress).address
            if (!perIpCount.asMap().containsKey(inetAddress)) {
                perIpCount.put(inetAddress, 0.toByte()) /* TODO IpPerSec Counter ++ */
            }
            /*
            Blacklisted -> channel.close(); return;
             */
            val pipeline = ch.pipeline()
            /* ReverseProxy -> Pipeline.register(pipeline); */
            val listener = ch.attr(PipelineUtils.LISTENER).get()

            // TODO TCP_FAST_OPEN. Add it configurable to config.
            if (System.getProperty("os.name").lowercase(Locale.getDefault()).contains("win")) {
                logWarn("Windows不支持.TCP-FAST-OPEN")
            } else {
                // ch.config().setOptions(ChannelOption.TCP_FASTOPEN, ?);
            }

            /*
            TODO:
            PlayerHandler

            Event: ClientConnectEvent isCancelled() (Cancellable); ch.close();
             */
            PipelineUtils.BASE.initChannel(ch)
            pipeline.replace(PipelineUtils.FRAME_DECODER, PipelineUtils.FRAME_DECODER, VarIntFrameDecoder())
            pipeline.replace(PipelineUtils.TIMEOUT_HANDLER, PipelineUtils.TIMEOUT_HANDLER, TimeoutHandler(30, TimeUnit.SECONDS))
            pipeline.replace(PipelineUtils.BOSS_HANDLER, PipelineUtils.BOSS_HANDLER, InboundHandler())
            pipeline.addBefore(PipelineUtils.FRAME_DECODER, PipelineUtils.LEGACY_DECODER, LegacyDecoder())
            pipeline.addAfter(
                PipelineUtils.FRAME_DECODER,
                PipelineUtils.PACKET_DECODER,
                MinecraftDecoder(Protocol.HANDSHAKE, true, protocol)
            )
            pipeline.addAfter(
                PipelineUtils.FRAME_PREPENDER,
                PipelineUtils.PACKET_ENCODER,
                MinecraftEncoder(Protocol.HANDSHAKE, true, protocol)
            )
            pipeline.addBefore(PipelineUtils.FRAME_PREPENDER, PipelineUtils.LEGACY_KICKER, legacyKick)
        } finally { if (!ctx.isRemoved) { ctx.pipeline().remove(this) } }
    }
}
