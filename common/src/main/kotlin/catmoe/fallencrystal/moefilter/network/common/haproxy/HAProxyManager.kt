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

package catmoe.fallencrystal.moefilter.network.common.haproxy

import catmoe.fallencrystal.translation.logger.CubeLogger
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import com.github.benmanes.caffeine.cache.Caffeine
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level

object HAProxyManager {

    val enabled = AtomicBoolean(false)
    private val allowAddress = Caffeine.newBuilder().build<String, Boolean>()
    private const val NAME = "moe-haproxy-decoder"

    fun load() {
        this.enabled.set(LocalConfig.getConfig().getBoolean("ha-proxy.enable"))
        this.allowAddress.invalidateAll()
        for (it in LocalConfig.getConfig().getStringList("ha-proxy.list")) {
            try {
                val address = InetAddress.getByName(it)
                this.allowAddress.put(address.hostAddress, true)
            } catch (_: UnknownHostException) {
                CubeLogger.log(Level.WARNING, "[HAProxy] Cannot whitelist: $it, Please check your host or address.")
            }
        }
    }

    fun handle(ctx: ChannelHandlerContext) {
        val address = (ctx.pipeline().channel().remoteAddress() as InetSocketAddress).address.hostAddress
        if (enabled.get() && allowAddress.getIfPresent(address) == true) ctx.pipeline().addBefore(ctx.name(), NAME, HAProxyMessageDecoder())
    }

}