package catmoe.fallencrystal.moefilter.api.logger

import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import net.md_5.bungee.BungeeCord

class InitLogger {
    private val ascii: List<String> = listOf(
        """ _____ ______   ________  _______   ________ ___  ___   _________  _______   ________     """,
        """|\   _ \  _   \|\   __  \|\  ___ \ |\  _____\\  \|\  \ |\___   ___\\  ___ \ |\   __  \    """,
        """\ \  \\\__\ \  \ \  \|\  \ \   __/|\ \  \__/\ \  \ \  \\|___ \  \_\ \   __/|\ \  \|\  \   """,
        """ \ \  \\|__| \  \ \  \\\  \ \  \_|/_\ \   __\\ \  \ \  \    \ \  \ \ \  \_|/_\ \   _  _\  """,
        """  \ \  \    \ \  \ \  \\\  \ \  \_|\ \ \  \_| \ \  \ \  \____\ \  \ \ \  \_|\ \ \  \\  \| """,
        """   \ \__\    \ \__\ \_______\ \_______\ \__\   \ \__\ \_______\ \__\ \ \_______\ \__\\ _\ """,
        """    \|__|     \|__|\|_______|\|_______|\|__|    \|__|\|_______|\|__|  \|_______|\|__|\|__|""",
        """                                                                                          """
    )

    init { ascii.forEach { MessageUtil.logInfo(it) } }

    private var useWaterfallLogger = false

    private val logger = try { useWaterfallLogger=true; io.github.waterfallmc.waterfall.log4j.WaterfallLogger.create(); }
    catch(ex: NoClassDefFoundError) { useWaterfallLogger=false; BungeeCord.getInstance().logger }

    fun onLoad() {
        logger.filter = LoggerManager
        if (useWaterfallLogger) { MessageUtil.logInfo("[MoeFilter] Detected Waterfall log4j logger. use it for main logger.") }
        MessageUtil.logInfo("[MoeFilter] LoggerManager are successfully loaded.")
    }

    fun onUnload() {
        logger.filter = null
        MessageUtil.logInfo("[MoeFilter] Unloaded logger filter.")
    }
}