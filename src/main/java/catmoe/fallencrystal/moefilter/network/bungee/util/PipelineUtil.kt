package catmoe.fallencrystal.moefilter.network.bungee.util

import com.github.benmanes.caffeine.cache.Caffeine
import io.netty.channel.ChannelHandlerContext
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer

object PipelineUtil {
    private val cache = Caffeine.newBuilder().build<ChannelHandlerContext, String>()
    private val playerCache = Caffeine.newBuilder().build<String, ChannelHandlerContext>()


    private val bungee = ProxyServer.getInstance()

    fun getPlayer(ctx: ChannelHandlerContext): ProxiedPlayer? { return bungee.getPlayer(cache.getIfPresent(ctx)) }

    fun getChannelHandler(player: ProxiedPlayer): ChannelHandlerContext? { return playerCache.getIfPresent(player.name) }

    fun putChannelHandler(ctx: ChannelHandlerContext, name: String) { cache.put(ctx, name); playerCache.put(name, ctx) }

    fun invalidateChannel(player: ProxiedPlayer) {
        val ctx = playerCache.getIfPresent(player.name)
        playerCache.invalidate(player.name)
        cache.invalidate(ctx ?: return)
    }
}