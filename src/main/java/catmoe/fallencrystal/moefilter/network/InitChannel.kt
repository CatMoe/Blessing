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

package catmoe.fallencrystal.moefilter.network

import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.network.bungee.limbo.MoeLimbo
import catmoe.fallencrystal.moefilter.network.bungee.limbo.netty.LimboPipeline
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.BungeePipeline
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.botfilter.BotFilterPipeline
import catmoe.fallencrystal.moefilter.network.bungee.util.ReflectionUtils
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import net.md_5.bungee.BungeeCord
import java.util.concurrent.atomic.AtomicBoolean

class InitChannel {

    private val knownIncompatibilitiesBungee = listOf("NullCordX", "XCord", "BetterBungee")
    private val knownIncompatibilitiesPlugin = listOf("AntiAttackRL", "HAProxyDetector", "JH_AntiBot", "nAntiBot", "BotSentry")

    private val bungee = BungeeCord.getInstance()
    private var isProxyProtocol = false
    private var pipeline: ChannelInitializer<Channel> = BungeePipeline()

    fun initPipeline() {
        log("Starting inject MoeFilter Pipeline...")
        val proxyName = bungee.name
        bungee.getConfig().listeners.forEach { if (it.isProxyProtocol) { isProxyProtocol = true } }
        if (isProxyProtocol) {
            log("<red>PIPELINE mode is incompatibility proxy_protocol! Please switch to EVENT mode.")
        }
        for (it in knownIncompatibilitiesBungee) {
            if (it.contains(proxyName)) {
                log("<red>Failed to inject because incompatibilities for $it bungeecord fork!")
                bungee.stop()
                return
            }
        }
        for (it in knownIncompatibilitiesPlugin) {
            if (bungee.pluginManager.getPlugin(it) != null) {
                log("<red>Failed to inject because the plugin $it is competing for the pipeline. Please unload that plugin first.")
                bungee.stop()
                return
            }
        }
        if (proxyName.contains("BotFilter")) {
            log("BotFilter is detected. Using compatibilities choose for it.")
            pipeline=BotFilterPipeline()
        }
        if (LocalConfig.getConfig().getBoolean("debug")) {
            log("Debug mode is on. Player now will connect to MoeLimbo instead of bungeecord itself.")
            pipeline=LimboPipeline()
            MoeLimbo.initDimension()
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