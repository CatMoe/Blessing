package catmoe.fallencrystal.moefilter.network.bungee.pipeline

import io.netty.channel.ChannelHandlerContext
import lombok.RequiredArgsConstructor

@RequiredArgsConstructor
class BungeePipeline : AbstractPipeline(), IPipeline {
    override fun handlerAdded(ctx: ChannelHandlerContext) { try { super.handlerAdded(ctx) } finally { if (!ctx.isRemoved) { ctx.pipeline().remove(this) } } }
}
