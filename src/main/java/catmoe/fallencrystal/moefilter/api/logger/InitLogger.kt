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

    init {
        try {
            io.github.waterfallmc.waterfall.log4j.WaterfallLogger.create().filter = LoggerManager
            ascii.forEach { MessageUtil.logInfo(it) }
            MessageUtil.logInfo("[MoeFilter] Detected Waterfall logger. using it for console filter.")
        } catch (ex: ClassNotFoundException) {
            ascii.forEach { MessageUtil.logInfo(it) }
            BungeeCord.getInstance().logger.filter = LoggerManager
        }
    }
}