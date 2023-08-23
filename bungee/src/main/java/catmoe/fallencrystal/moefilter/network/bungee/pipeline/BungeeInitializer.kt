package catmoe.fallencrystal.moefilter.network.bungee.pipeline

import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher
import io.netty.channel.ChannelHandlerContext

class BungeeInitializer : AbstractInitializer(), IPipeline {
    override fun handlerAdded(ctx: ChannelHandlerContext) { try { super.handlerAdded(ctx) } finally { if (!ctx.isRemoved) { ctx.pipeline().remove(this) } } }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) { ExceptionCatcher.handle(ctx!!.channel(), cause!!) }
}