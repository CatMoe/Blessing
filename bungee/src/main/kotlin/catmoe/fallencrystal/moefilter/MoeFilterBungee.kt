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

import catmoe.fallencrystal.moefilter.api.logger.InitLogger
import catmoe.fallencrystal.moefilter.event.PluginUnloadEvent
import catmoe.fallencrystal.moefilter.network.bungee.util.WorkingMode
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.AsyncLoader
import catmoe.fallencrystal.translation.CPlatform
import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.platform.*
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import java.io.File

@Platform(ProxyPlatform.BUNGEE)
class MoeFilterBungee : Plugin(), PlatformLoader {

    private val initLogger = InitLogger()

    private val viaLoader = CPlatform(this)

    init {
        instance=this
        viaLoader.readyLoad()
    }

    override fun onEnable() {
        load()
    }

    override fun onDisable() {
        viaLoader.whenUnload()
        initLogger.onUnload()
        EventManager.callEvent(PluginUnloadEvent())
    }

    private fun load() {
        val loader = AsyncLoader(this, viaLoader)
        initLogger.onLoad()
        loader.load()
    }

    override fun onLoad() {
        MessageUtil.logInfo("[MoeFilter] Using MoeFilter API")
    }

    companion object {
        lateinit var instance: MoeFilterBungee
            private set
        var mode: WorkingMode? = null
    }

    override fun getPluginFolder(): File {
        return dataFolder
    }

    override fun getPluginInstance(): PlatformLoader {
        return instance
    }

    override fun whenLoad() {  }

    override fun whenUnload() {  }

    override fun pluginVersion(): String {
        return this.description.version
    }

    override fun readyLoad() {  }

    override fun getPlatformLogger(): SimpleLogger {
        return SimpleLogger(BungeeCord.getInstance().logger)
    }

    override fun getProxyServer(): MoeProxyServer {
        val proxy = ProxyServer.getInstance()
        return MoeProxyServer(ProxyPlatform.BUNGEE, proxy)
    }
}