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
        val loader = AsyncLoader(this, viaLoader)
        initLogger.onLoad()
        loader.load()
    }

    override fun onDisable() {
        viaLoader.whenUnload()
        initLogger.onUnload()
        EventManager.callEvent(PluginUnloadEvent())
    }

    override fun onLoad() = MessageUtil.logInfo("[MoeFilter] Using MoeFilter API")

    companion object {
        lateinit var instance: MoeFilterBungee
            private set
        var mode: WorkingMode? = null
    }

    override fun getPluginFolder(): File = dataFolder

    override fun getPluginInstance() = instance

    override fun whenLoad() {
        // Do not need that.
    }

    override fun whenUnload() {
        // Do not need that.
    }

    override fun pluginVersion(): String = this.description.version

    override fun readyLoad() {
        // Do not need that.
    }

    override fun getPlatformLogger() = SimpleLogger(BungeeCord.getInstance().logger)

    override fun getProxyServer() = MoeProxyServer(ProxyPlatform.BUNGEE, ProxyServer.getInstance())
}