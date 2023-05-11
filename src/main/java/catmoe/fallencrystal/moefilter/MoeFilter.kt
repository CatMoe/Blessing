package catmoe.fallencrystal.moefilter

import catmoe.fallencrystal.moefilter.api.logger.LoggerManager
import catmoe.fallencrystal.moefilter.util.ExceptionCatcher
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

class MoeFilter : Plugin() {

    private val proxy = ProxyServer.getInstance()

    override fun onEnable() {
        registerLogger()
        TODO()
    }

    override fun onDisable() {
        LoggerManager.unregisterLogger(ExceptionCatcher())
    }

    private fun registerLogger() {
        proxy.logger.filter = LoggerManager
        LoggerManager.registerLogger(ExceptionCatcher())
    }

}