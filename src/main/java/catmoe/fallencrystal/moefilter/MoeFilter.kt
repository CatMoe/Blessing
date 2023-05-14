package catmoe.fallencrystal.moefilter

import catmoe.fallencrystal.moefilter.api.command.LoadCommand
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

        LoadCommand(this).load()
        registerLogger()
    }

    override fun onDisable() {
    }

    private fun registerLogger() {
        proxy.logger.filter = LoggerManager
        LoggerManager.registerFilter(LogHandler())
    }

    fun getInstance():MoeFilter { return this }

}