package catmoe.fallencrystal.moefilter.network.bungee.pipeline

import catmoe.fallencrystal.moefilter.network.bungee.handler.BungeeHandler
import catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher.handle
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline

object MoeChannelHandler : IPipeline {
    @JvmField
    val EXCEPTION_HANDLER: ChannelHandler = PacketExceptionHandler()

    @Sharable
    private class PacketExceptionHandler : ChannelDuplexHandler() {
        @Deprecated("Deprecated in Java", ReplaceWith("handle(ctx.channel(), cause)", "catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher.handle"))
        @Throws(Exception::class)
        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            handle(ctx.channel(), cause)
        }
    }

    private val MOEFILTER_HANDLER = BungeeHandler()

    fun register(pipeline: ChannelPipeline) { pipeline.addFirst(IPipeline.HANDLER, MOEFILTER_HANDLER) }
}
