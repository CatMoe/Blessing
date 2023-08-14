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
package catmoe.fallencrystal.moefilter

import catmoe.fallencrystal.moefilter.listener.VelocityBrandListener
import catmoe.fallencrystal.moefilter.platform.*
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerClientBrandEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger
import java.io.File
import java.nio.file.Path

@Suppress("MemberVisibilityCanBePrivate")
@Platform(platform = ProxyPlatform.VELOCITY)
@Plugin(
    id = "moefilter",
    name = "MoeFilter",
    version = "0.1.2-Alpha",
    description = "❤ by CatMoe",
    authors = ["Fallen Crystal", "Shizoukia"],
    url = "www.miaomoe.net"
)
class MoeFilterVelocity @Inject constructor(
    val proxyServer: ProxyServer,
    val logger: Logger,
    @DataDirectory dataDirectory: Path
) : PlatformLoader {
    private val pluginFolder: File

    init {
        instance=this
        pluginFolder = dataDirectory.toFile()
        logger.warn("MoeFilter Velocity now under development. Many features are currently unavailable.")
    }

    @Subscribe
    fun proxyLoad(event: ProxyInitializeEvent) {
        cPlatform.whenLoad()
        proxyServer.eventManager.register(this, PlayerClientBrandEvent::class.java, VelocityBrandListener(this))
    }

    private val cPlatform = CPlatform(this)

    override fun getPluginFolder(): File { return pluginFolder }

    override fun getPluginInstance(): PlatformLoader { return this }
    override fun readyLoad() {}

    override fun whenLoad() {}
    override fun whenUnload() {}
    override fun pluginVersion(): String { return "0.1.2-Alpha" }

    override fun getPlatformLogger(): SimpleLogger { return SimpleLogger(logger) }

    override fun getProxyServer(): MoeProxyServer {
        return MoeProxyServer(ProxyPlatform.VELOCITY, proxyServer)
    }

    companion object {
        lateinit var instance: MoeFilterVelocity
            private set
    }
}
