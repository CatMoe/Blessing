package catmoe.fallencrystal.moefilter.network.bungee.handler

import catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher
import io.netty.channel.ChannelHandlerContext
import net.md_5.bungee.netty.HandlerBoss

class InboundHandler : HandlerBoss() {
    @Deprecated("Deprecated in Java", ReplaceWith("ExceptionCatcher.handle(ctx.channel(), cause)", "catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher"))
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { ExceptionCatcher.handle(ctx.channel(), cause) }
}