package catmoe.fallencrystal.moefilter.api.logger

import catmoe.fallencrystal.moefilter.api.logger.LoggerManager.setType
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import net.md_5.bungee.BungeeCord

class InitLogger {
    private val ascii: List<String> = listOf(
        """&b_____ ______   ________  _______   ________ ___  ___   _________  _______   ________     """,
        """&b|\   _ \  _   \|\   __  \|\  ___ \ |\  _____\\  \|\  \ |\___   ___\\  ___ \ |\   __  \    """,
        """&b\ \  \\\__\ \  \ \  \|\  \ \   __/|\ \  \__/\ \  \ \  \\|___ \  \_\ \   __/|\ \  \|\  \   """,
        """&b \ \  \\|__| \  \ \  \\\  \ \  \_|/_\ \   __\\ \  \ \  \    \ \  \ \ \  \_|/_\ \   _  _\  """,
        """&b  \ \  \    \ \  \ \  \\\  \ \  \_|\ \ \  \_| \ \  \ \  \____\ \  \ \ \  \_|\ \ \  \\  \ """,
        """&b   \ \__\    \ \__\ \_______\ \_______\ \__\   \ \__\ \_______\ \__\ \ \_______\ \__\\ _\ """,
        """&b    \|__|     \|__|\|_______|\|_______|\|__|    \|__|\|_______|\|__|  \|_______|\|__|\|__|""",
        """&b                                                                                          """
    )

    init { ascii.forEach { MessageUtil.logInfo(it) } }

    private var useWaterfallLogger = false

    private val logger = try { useWaterfallLogger=true; io.github.waterfallmc.waterfall.log4j.WaterfallLogger.create(); }
    catch(ex: NoClassDefFoundError) { useWaterfallLogger=false; BungeeCord.getInstance().logger }

    fun onLoad() {
        logger.filter = LoggerManager
        if (useWaterfallLogger) {
            MessageUtil.logInfo("[MoeFilter] Detected Waterfall log4j logger. use it for main logger.")
            setType(BCLogType.WATERFALL)
        } else {
            MessageUtil.logInfo("[MoeFilter] Using tradition java logger. Waterfall or its fork is recommended.")
            setType(BCLogType.BUNGEECORD)
        }
        MessageUtil.logInfo("[MoeFilter] LoggerManager are successfully loaded.")
    }

    fun onUnload() {
        logger.filter = null
        MessageUtil.logInfo("[MoeFilter] Unloaded logger filter.")
    }
}