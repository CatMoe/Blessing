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
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import net.md_5.bungee.BungeeCord

class InitLogger {
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

    private var useWaterfallLogger = false

    private val logger = try { useWaterfallLogger=true; io.github.waterfallmc.waterfall.log4j.WaterfallLogger.create(); }
    catch(ex: NoClassDefFoundError) { useWaterfallLogger=false; BungeeCord.getInstance().logger }

    fun onLoad() {
        ascii.forEach { MessageUtil.logInfo(it) }
        logger.filter = LoggerManager
        if (useWaterfallLogger) {
            MessageUtil.logInfo("[MoeFilter] <green>Detected Waterfall log4j logger. use it for main logger.")
            setType(BCLogType.WATERFALL)
        } else {
            MessageUtil.logInfo("[MoeFilter] <yellow>Using tradition java logger. Waterfall or its fork is recommended.")
            setType(BCLogType.BUNGEECORD)
        }
        MessageUtil.logInfo("[MoeFilter] <green>LoggerManager are successfully loaded.")
    }

    fun onUnload() {
        logger.filter = null
        MessageUtil.logInfo("[MoeFilter] Unloaded logger filter.")
    }
}