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