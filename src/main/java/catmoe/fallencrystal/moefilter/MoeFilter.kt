package catmoe.fallencrystal.moefilter

import catmoe.fallencrystal.moefilter.api.command.Command
import catmoe.fallencrystal.moefilter.api.command.OCommand
import catmoe.fallencrystal.moefilter.api.command.impl.HelpCommand
import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.logger.LoggerManager
import catmoe.fallencrystal.moefilter.util.ExceptionCatcher
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

class MoeFilter : Plugin() {

    private val proxy = ProxyServer.getInstance()

    override fun onEnable() {
        EventManager // 初始化
        registerCommand()
        registerLogger()
        TODO()
    }

    override fun onDisable() {
        LoggerManager.unregisterLogger(ExceptionCatcher())
    }

    private fun registerCommand() {
        val command = Command("moefilter", "", "ab", "antibot", "filter", "moefilter", "mf")
        proxy.pluginManager.registerCommand(this, command)
        OCommand.register(HelpCommand())
    }

    private fun registerLogger() {
        proxy.logger.filter = LoggerManager
        LoggerManager.registerLogger(ExceptionCatcher())
    }

    fun getInstance():MoeFilter { return this }

}