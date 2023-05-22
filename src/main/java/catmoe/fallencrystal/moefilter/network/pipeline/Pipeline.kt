package catmoe.fallencrystal.moefilter.network.pipeline

import catmoe.fallencrystal.moefilter.network.handler.BungeeHandler
import catmoe.fallencrystal.moefilter.network.util.ChannelExceptionCatcher.handle
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
import lombok.experimental.UtilityClass

@UtilityClass
class Pipeline : IPipeline {
    val EXCEPTION_HANDLER: ChannelHandler = PacketExceptionHandler()

    @Sharable
    private inner class PacketExceptionHandler : ChannelDuplexHandler() {
        @Deprecated("Deprecated in Java", ReplaceWith(
            "handle(ctx.channel(), cause)",
            "catmoe.fallencrystal.moefilter.network.util.ChannelExceptionCatcher.handle"))
        @Throws(Exception::class)
        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { handle(ctx.channel(), cause) }
    }

    private val moeFilterHandler: ChannelHandler = BungeeHandler()
    fun register(pipeline: ChannelPipeline) { pipeline.addFirst(IPipeline.HANDLER, moeFilterHandler) }
}
