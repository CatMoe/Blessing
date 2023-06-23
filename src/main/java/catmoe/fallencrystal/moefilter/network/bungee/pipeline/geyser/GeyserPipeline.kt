package catmoe.fallencrystal.moefilter.network.bungee.pipeline.geyser

import io.netty.channel.Channel
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.connection.InitialHandler
import net.md_5.bungee.netty.HandlerBoss
import net.md_5.bungee.netty.PipelineUtils
import net.md_5.bungee.protocol.*

class GeyserPipeline {
    private val legacyKicker = KickStringWriter()

    @Throws(Exception::class)
    fun handle(channel: Channel, protocol: Int) {
        if (channel.remoteAddress() == null) { channel.close(); return }
        val listener = channel.attr(PipelineUtils.LISTENER).get()
        PipelineUtils.BASE.initChannel(channel)
        val pipeline = channel.pipeline()
        pipeline.addBefore(PipelineUtils.FRAME_DECODER, PipelineUtils.LEGACY_DECODER, LegacyDecoder())
        pipeline.addAfter(PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, MinecraftDecoder(Protocol.HANDSHAKE, true, protocol))
        pipeline.addAfter(PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, MinecraftEncoder(Protocol.HANDSHAKE, true, protocol))
        pipeline.addBefore(PipelineUtils.FRAME_PREPENDER, PipelineUtils.LEGACY_KICKER, legacyKicker)
        channel.pipeline().get(HandlerBoss::class.java).setHandler(InitialHandler(BungeeCord.getInstance(), listener))

        if (listener.isProxyProtocol) { channel.pipeline().addFirst(HAProxyMessageDecoder()) }
    }
}
