package catmoe.fallencrystal.moefilter

import catmoe.fallencrystal.moefilter.api.command.impl.test.log.LogHandler
import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.PluginUnloadEvent
import catmoe.fallencrystal.moefilter.api.logger.InitLogger
import catmoe.fallencrystal.moefilter.api.logger.LoggerManager
import catmoe.fallencrystal.moefilter.util.plugin.AsyncLoader
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

class MoeFilter : Plugin() {

    private val proxy = ProxyServer.getInstance()
    private val initLogger = InitLogger()

    override fun onEnable() {
        FilterPlugin.setPlugin(this)
        FilterPlugin.setDataFolder(dataFolder)
        initLogger.onLoad()
        AsyncLoader(this)
    }

    override fun onDisable() { EventManager.triggerEvent(PluginUnloadEvent()); initLogger.onUnload() }

    private fun registerLogger() {
        InitLogger()
        LoggerManager.registerFilter(LogHandler())
    }
}