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

package catmoe.fallencrystal.moefilter.network

import catmoe.fallencrystal.moefilter.network.bungee.ReflectionUtils
import catmoe.fallencrystal.moefilter.network.bungee.initializer.BungeeInitializer
import catmoe.fallencrystal.moefilter.network.bungee.initializer.botfilter.BotFilterInitializer
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import net.md_5.bungee.BungeeCord
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

class InitializerInjector {

    private val bungee = BungeeCord.getInstance()
    private var pipeline: ChannelInitializer<Channel> = BungeeInitializer()

    fun initPipeline() {
        log("Starting inject MoeFilter Pipeline...")
        val proxyName = bungee.name
        bungee.getConfig().listeners
            ?: {
                try { throw NullPointerException("Listener cannot be null! Please report this issue to CatMoe!") }
                catch (ex: NullPointerException) { ex.printStackTrace() }
                MessageUtil.logError("[MoeFilter] Cannot start BungeeCord because listener is null")
                Thread.sleep(1000)
                exitProcess(1)
            }
        if (proxyName.contains("BotFilter")) {
            log("BotFilter is detected. Using compatibilities choose for it.")
            pipeline=BotFilterInitializer()
        }
        try {
            if (!inject(pipeline).get()) {
                log("<red>Failed to inject pipeline. Please report this issue for CatMoe!")
            } else {
                log("<green>Pipeline inject successfully.")
            }
        } catch (err: UnsupportedClassVersionError) {
            err.printStackTrace(); bungee.stop()
        }
    }

    private fun inject(pipeline: ChannelInitializer<Channel>): AtomicBoolean { return ReflectionUtils().inject(pipeline) }

    private fun log(text: String) { MessageUtil.logWarn("[MoeFilter] [Pipeline] $text") }
}