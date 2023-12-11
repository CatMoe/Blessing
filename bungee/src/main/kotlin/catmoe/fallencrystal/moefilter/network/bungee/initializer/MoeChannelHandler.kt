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

package catmoe.fallencrystal.moefilter.network.bungee.initializer

import catmoe.fallencrystal.moefilter.common.state.StateManager
import catmoe.fallencrystal.moefilter.network.bungee.handler.ByteLimiter
import catmoe.fallencrystal.moefilter.network.bungee.util.WorkingMode
import catmoe.fallencrystal.moefilter.network.common.ExceptionCatcher.handle
import catmoe.fallencrystal.moefilter.util.plugin.AsyncLoader
import catmoe.fallencrystal.translation.utils.config.LocalConfig
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

    val BYTE_LIMITER = ByteLimiter()

    val callInitEvent = LocalConfig.getAntibot().getBoolean("call-connect-event")
    val injectPacketListener = AsyncLoader.instance.mode != WorkingMode.DISABLED
}
