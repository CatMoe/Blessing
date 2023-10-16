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

package catmoe.fallencrystal.moefilter.api.logger

import catmoe.fallencrystal.moefilter.api.logger.LoggerManager.setType
import catmoe.fallencrystal.translation.logger.CubeLogger
import catmoe.fallencrystal.translation.logger.ICubeLogger
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import net.kyori.adventure.text.Component
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.chat.BaseComponent
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
        logger.log(level, (ComponentUtil.toBaseComponents(component) as? BaseComponent)?.toLegacyText())
    }

    override fun logInstance(): Any {
        return this
    }
}