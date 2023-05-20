package catmoe.fallencrystal.moefilter.network.handler

import catmoe.fallencrystal.moefilter.network.util.ChannelExceptionCatcher
import io.netty.channel.ChannelHandlerContext
import net.md_5.bungee.netty.HandlerBoss

class InboundHandler : HandlerBoss() {
    @Deprecated("Deprecated in Java", ReplaceWith(
        "ChannelExceptionCatcher.handle(ctx!!.channel(), cause)",
        "catmoe.fallencrystal.moefilter.network.util.ChannelExceptionCatcher"
    ))
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) { ChannelExceptionCatcher.handle(ctx!!.channel(), cause) }
}