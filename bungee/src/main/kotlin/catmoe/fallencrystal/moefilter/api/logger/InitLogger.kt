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

package catmoe.fallencrystal.moefilter.api.logger

import catmoe.fallencrystal.moefilter.api.logger.LoggerManager.setType
import catmoe.fallencrystal.translation.logger.CubeLogger
import catmoe.fallencrystal.translation.logger.ICubeLogger
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import net.kyori.adventure.text.Component
import net.md_5.bungee.BungeeCord
import java.util.logging.Level

class InitLogger : ICubeLogger {
    private val ascii: List<String> = listOf(
        """<aqua>_____ ______   ________  _______   ________ ___  ___   _________  _______   ________     """,
        """<aqua>|\   _ \  _   \|\   __  \|\  ___ \ |\  _____\\  \|\  \ |\___   ___\\  ___ \ |\   __  \    """,
        """<aqua>\ \  \\\__\ \  \ \  \|\  \ \   __/|\ \  \__/\ \  \ \  \\|___ \  \_\ \   __/|\ \  \|\  \   """,
        """<aqua> \ \  \\|__| \  \ \  \\\  \ \  \_|/_\ \   __\\ \  \ \  \    \ \  \ \ \  \_|/_\ \   _  _\  """,
        """<aqua>  \ \  \    \ \  \ \  \\\  \ \  \_|\ \ \  \_| \ \  \ \  \____\ \  \ \ \  \_|\ \ \  \\  \ """,
        """<aqua>   \ \__\    \ \__\ \_______\ \_______\ \__\   \ \__\ \_______\ \__\ \ \_______\ \__\\ _\ """,
        """<aqua>    \|__|     \|__|\|_______|\|_______|\|__|    \|__|\|_______|\|__|  \|_______|\|__|\|__|""",
        """<aqua>                                                                                          """
    )

    init { CubeLogger.logger=this }

    private var useWaterfallLogger = false

    private val logger = try { useWaterfallLogger=true; io.github.waterfallmc.waterfall.log4j.WaterfallLogger.create(); }
    catch(ex: NoClassDefFoundError) { useWaterfallLogger=false; BungeeCord.getInstance().logger }

    fun onLoad() {
        ascii.forEach { this.log(Level.INFO, ComponentUtil.parse(it)) }
        logger.filter = LoggerManager
        if (useWaterfallLogger) {
            this.log(Level.INFO, ComponentUtil.parse("[MoeFilter] <green>Detected Waterfall log4j logger. use it for main logger."))
            setType(BCLogType.WATERFALL)
        } else {
            this.log(Level.INFO, ComponentUtil.parse("[MoeFilter] <yellow>Using tradition java logger. Waterfall or its fork is recommended."))
            setType(BCLogType.BUNGEECORD)
        }
        this.log(Level.INFO, ComponentUtil.parse("[MoeFilter] <green>LoggerManager are successfully loaded."))
    }

    fun onUnload() {
        logger.filter = null
        this.log(Level.INFO, ComponentUtil.parse("[MoeFilter] Unloaded logger filter."))
    }

    override fun log(level: Level, message: String) {
        logger.log(level, message)
    }

    override fun log(level: Level, component: Component) {
        logger.log(level, ComponentUtil.toBaseComponents(component)?.toLegacyText())
    }

    override fun logInstance(): Any {
        return this
    }
}