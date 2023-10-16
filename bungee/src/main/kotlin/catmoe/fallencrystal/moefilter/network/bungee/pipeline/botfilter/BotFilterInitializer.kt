package catmoe.fallencrystal.moefilter.network.bungee.pipeline.botfilter

import catmoe.fallencrystal.moefilter.network.bungee.pipeline.AbstractInitializer
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.IPipeline
import catmoe.fallencrystal.moefilter.network.bungee.util.event.EventCaller
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
import lombok.RequiredArgsConstructor
import net.md_5.bungee.api.config.ListenerInfo
import net.md_5.bungee.netty.PipelineUtils
import net.md_5.bungee.protocol.Varint21FrameDecoder

@RequiredArgsConstructor
class BotFilterInitializer : AbstractInitializer(), IPipeline {

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        try {
            // Use original VarInt21FrameDecoder for BotFilter.
            super.handlerAdded(ctx)
            if (ctx.channel().isActive) { ctx.pipeline().replace(PipelineUtils.FRAME_DECODER, PipelineUtils.FRAME_DECODER, Varint21FrameDecoder()) }
        } finally { if (!ctx.isRemoved) { ctx.pipeline().remove(this) } }
    }

    override fun connectToBungee(
        ctx: ChannelHandlerContext,
        pipeline: ChannelPipeline,
        channel: Channel,
        eventCaller: EventCaller,
        listener: ListenerInfo
    ) {
        super.connectToBungee(ctx, pipeline, channel, eventCaller, listener)
        pipeline.replace(PipelineUtils.FRAME_DECODER, PipelineUtils.FRAME_DECODER, Varint21FrameDecoder())
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) { ExceptionCatcher.handle(ctx!!.channel(), cause!!) }
}