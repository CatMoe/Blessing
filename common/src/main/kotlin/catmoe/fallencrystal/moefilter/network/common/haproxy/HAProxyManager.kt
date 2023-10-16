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