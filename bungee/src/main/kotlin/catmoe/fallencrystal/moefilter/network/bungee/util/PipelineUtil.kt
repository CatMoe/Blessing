/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
