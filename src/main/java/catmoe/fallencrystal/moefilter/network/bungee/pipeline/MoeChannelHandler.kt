package catmoe.fallencrystal.moefilter.network.bungee.pipeline

import catmoe.fallencrystal.moefilter.network.bungee.handler.BungeeHandler
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher.handle
import com.github.benmanes.caffeine.cache.Caffeine
import io.netty.channel.*
import io.netty.channel.ChannelHandler.Sharable
import java.util.concurrent.TimeUnit

object MoeChannelHandler : IPipeline {
    @JvmField
    val EXCEPTION_HANDLER: ChannelHandler = PacketExceptionHandler()

    val sentHandshake = Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.SECONDS)
        .build<Channel, Boolean>()

    @Sharable
    private class PacketExceptionHandler : ChannelDuplexHandler() {
        @Suppress("OVERRIDE_DEPRECATION")
        @Throws(Exception::class)
        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { handle(ctx.channel(), cause) }
    }

    private val MOEFILTER_HANDLER = BungeeHandler()

    fun register(pipeline: ChannelPipeline) { pipeline.addFirst(IPipeline.HANDLER, MOEFILTER_HANDLER) }
}
