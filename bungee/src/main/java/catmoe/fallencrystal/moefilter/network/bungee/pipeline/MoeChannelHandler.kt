package catmoe.fallencrystal.moefilter.network.bungee.pipeline

import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.state.StateManager
import catmoe.fallencrystal.moefilter.network.bungee.handler.ByteBufHandler
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher.handle
import com.github.benmanes.caffeine.cache.Caffeine
import io.netty.channel.*
import io.netty.channel.ChannelHandler.Sharable
import net.md_5.bungee.BungeeCord
import java.util.concurrent.TimeUnit

object MoeChannelHandler : IPipeline {
    @JvmField
    val EXCEPTION_HANDLER: ChannelHandler = PacketExceptionHandler()

    val sentHandshake = Caffeine.newBuilder()
        .expireAfterAccess(30, TimeUnit.SECONDS)
        .build<Channel, Boolean>()

    private val defaultTimeout = BungeeCord.getInstance().config.timeout.toLong()
    private val a = LocalConfig.getAntibot().getLong("dynamic-timeout")
    private val timeoutInAttack = if (a == (-1).toLong()) defaultTimeout else a

    val dynamicTimeout: Long get() = if (StateManager.inAttack.get()) timeoutInAttack else defaultTimeout

    @Sharable
    private class PacketExceptionHandler : ChannelDuplexHandler() {
        @Suppress("OVERRIDE_DEPRECATION")
        @Throws(Exception::class)
        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) { handle(ctx.channel(), cause) }
    }

    private val MOEFILTER_HANDLER = ByteBufHandler()

    fun register(pipeline: ChannelPipeline) { pipeline.addFirst(IPipeline.HANDLER, MOEFILTER_HANDLER) }
}
