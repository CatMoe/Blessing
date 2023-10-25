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

import catmoe.fallencrystal.moefilter.logger.VelocityLogger
import catmoe.fallencrystal.translation.CPlatform
import catmoe.fallencrystal.translation.logger.CubeLogger
import catmoe.fallencrystal.translation.platform.*
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
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
    version = "0.1.4",
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

    private val cPlatform = CPlatform(this)

    init {
        instance=this
        pluginFolder = dataDirectory.toFile()
        cPlatform.readyLoad()
        logger.warn("MoeFilter Velocity now under development. Many features are currently unavailable.")
    }

    @Subscribe
    @Suppress("UNUSED_PARAMETER")
    fun proxyLoad(event: ProxyInitializeEvent) {
        CubeLogger.logger=VelocityLogger(this, proxyServer, logger)
        cPlatform.whenLoad()
        proxyServer.eventManager.register(this, ConversionListener(this))
    }

    override fun getPluginFolder() = pluginFolder

    override fun getPluginInstance() = this
    override fun readyLoad() {
        // Do not have any impl need that.
    }

    override fun whenLoad() {
        // Do not have any impl need that.
    }
    override fun whenUnload() {
        // Do not have any impl need that.
    }
    override fun pluginVersion() = "0.1.4"

    override fun getPlatformLogger() = SimpleLogger(logger)

    override fun getProxyServer() = MoeProxyServer(ProxyPlatform.VELOCITY, proxyServer)

    companion object {
        lateinit var instance: MoeFilterVelocity
            private set
    }
}
