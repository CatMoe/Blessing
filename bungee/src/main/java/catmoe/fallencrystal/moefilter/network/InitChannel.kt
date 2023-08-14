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

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.BungeeInitializer
import catmoe.fallencrystal.moefilter.network.bungee.pipeline.botfilter.BotFilterInitializer
import catmoe.fallencrystal.moefilter.network.bungee.ReflectionUtils
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.config.ListenerInfo
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

class InitChannel {

    private val incompatibility = listOf(
        "NullCordX",
        "XCord",
        "BetterBungee",
        "AntiAttackRL",
        "HAProxyDetector",
        "JH_AntiBot",
        "nAntiBot",
        "BotSentry"
    )

    private val bungee = BungeeCord.getInstance()
    private var isProxyProtocol = false
    private var pipeline: ChannelInitializer<Channel> = BungeeInitializer()

    fun initPipeline() {
        log("Starting inject MoeFilter Pipeline...")
        val proxyName = bungee.name
        val listener: MutableCollection<ListenerInfo> = bungee.getConfig().listeners
            ?: if (LocalConfig.getConfig().getBoolean("fastboot")) {
                MoeFilterBungee.instance.injectPipelineAfterLoad.set(true)
                return
            } else {
                try { throw NullPointerException("Listener cannot be null! Please report this issue to CatMoe!") }
                catch (ex: NullPointerException) { ex.printStackTrace() }
                MessageUtil.logError("[MoeFilter] Cannot start bungeecord because listener is null")
                exitProcess(1)
            }
        listener.forEach { if (it.isProxyProtocol) { isProxyProtocol = true } }
        if (isProxyProtocol) {
            log("<red>PIPELINE mode is incompatibility proxy_protocol! Please switch to EVENT mode.")
            bungee.stop()
        }
        incompatibility.forEach {
            if (it.contains(proxyName) || bungee.pluginManager.getPlugin(it) != null) {
                log("$it plugin or bungeecord (forks) is not compatibility with MoeFilter PIPELINE mode.")
                log("Please switch to EVENT mode. or give up use MoeFilter.")
                bungee.stop(); return
            }
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