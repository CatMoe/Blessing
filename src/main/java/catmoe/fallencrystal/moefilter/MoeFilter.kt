package catmoe.fallencrystal.moefilter

import catmoe.fallencrystal.moefilter.api.command.Command
import catmoe.fallencrystal.moefilter.api.command.OCommand
import catmoe.fallencrystal.moefilter.api.command.impl.HelpCommand
import catmoe.fallencrystal.moefilter.api.command.impl.log.LogCommand
import catmoe.fallencrystal.moefilter.api.command.impl.log.LogHandler
import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.logger.LoggerManager
import catmoe.fallencrystal.moefilter.common.config.LoadConfig
import catmoe.fallencrystal.moefilter.util.FilterPlugin
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

class MoeFilter : Plugin() {

    private val proxy = ProxyServer.getInstance()

    override fun onEnable() {
        FilterPlugin.setPlugin(this)
        FilterPlugin.setDataFolder(dataFolder)

        LoadConfig

        EventManager // 初始化

        registerCommand()
        registerLogger()
    }

    override fun onDisable() {
    }

    private fun registerCommand() {
        val command = Command("moefilter", "", "ab", "antibot", "filter", "moefilter", "mf")
        proxy.pluginManager.registerCommand(this, command)
        OCommand.register(HelpCommand())
        OCommand.register(LogCommand())
    }

    private fun registerLogger() {
        proxy.logger.filter = LoggerManager
        LoggerManager.registerFilter(LogHandler())
    }

    fun getInstance():MoeFilter { return this }

}