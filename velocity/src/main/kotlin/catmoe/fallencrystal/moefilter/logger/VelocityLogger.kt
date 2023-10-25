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

package catmoe.fallencrystal.moefilter.logger

import catmoe.fallencrystal.moefilter.MoeFilterVelocity
import catmoe.fallencrystal.translation.logger.ICubeLogger
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import org.slf4j.Logger
import java.util.logging.Level

@Suppress("MemberVisibilityCanBePrivate")
class VelocityLogger(val plugin: MoeFilterVelocity, val proxyServer: ProxyServer, val logger: Logger) : ICubeLogger {

    override fun log(level: Level, message: String) {
        when (level) {
            Level.INFO -> logger.info(message)
            Level.WARNING -> logger.warn(message)
            Level.SEVERE -> logger.error(message)
        }
    }

    private val levelMap = mapOf(
        Level.INFO to "<white>",
        Level.WARNING to "<yellow>",
        Level.SEVERE to "<red>"
    )

    override fun log(level: Level, component: Component) {
        val orig = ComponentUtil.componentToRaw(component)
        val lev = levelMap[level] ?: "<white>"
        proxyServer.consoleCommandSource.sendMessage(ComponentUtil.parse("$lev $orig"))
    }

    override fun logInstance(): Any {
        return this
    }
}