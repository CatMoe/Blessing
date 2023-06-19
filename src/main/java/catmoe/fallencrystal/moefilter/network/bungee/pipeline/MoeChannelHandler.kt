package catmoe.fallencrystal.moefilter.network.bungee.pipeline

import catmoe.fallencrystal.moefilter.network.bungee.ExceptionCatcher.handle
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext

object MoeChannelHandler : IPipeline {
    @JvmField
    val EXCEPTION_HANDLER: ChannelHandler = PacketExceptionHandler()

    @Sharable
    private class PacketExceptionHandler : ChannelDuplexHandler() {
        @Deprecated("Deprecated in Java", ReplaceWith("handle(ctx.channel(), cause)", "catmoe.fallencrystal.moefilter.network.bungee.ExceptionCatcher.handle"))
        @Throws(Exception::class)
        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            handle(ctx.channel(), cause)
        }
    } // public void register(final ChannelPipeline pipeline) { pipeline.addFirst(HANDLER, ) }
}
