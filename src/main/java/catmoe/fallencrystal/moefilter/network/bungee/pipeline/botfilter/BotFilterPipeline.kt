package catmoe.fallencrystal.moefilter.network.bungee.pipeline.botfilter

import catmoe.fallencrystal.moefilter.network.bungee.pipeline.AbstractPipeline
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.IPipeline
import catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher
import io.netty.channel.ChannelHandlerContext
import lombok.RequiredArgsConstructor
import net.md_5.bungee.netty.PipelineUtils
import net.md_5.bungee.protocol.Varint21FrameDecoder

@RequiredArgsConstructor
class BotFilterPipeline : AbstractPipeline(), IPipeline {

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        try {
            // Use original Varint21FrameDecoder for BotFilter.
            super.handlerAdded(ctx)
            if (ctx.channel().isActive) { ctx.pipeline().replace(PipelineUtils.FRAME_DECODER, PipelineUtils.FRAME_DECODER, Varint21FrameDecoder()) }
        } finally { if (!ctx.isRemoved) { ctx.pipeline().remove(this) } }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) { ExceptionCatcher.handle(ctx!!.channel(), cause!!) }
}