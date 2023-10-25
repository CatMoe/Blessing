/*
 * Copyright (C) 2023-2023. CatMoe / MoeFilter Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
        val ctx: ChannelHandlerContext? = playerCache.getIfPresent(player.name)
        playerCache.invalidate(player.name)
        cache.invalidate(ctx ?: return)
    }
}
