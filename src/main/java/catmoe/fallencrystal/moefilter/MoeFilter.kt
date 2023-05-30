package catmoe.fallencrystal.moefilter

import catmoe.fallencrystal.moefilter.api.command.impl.test.log.LogHandler
import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.PluginReloadEvent
import catmoe.fallencrystal.moefilter.api.event.events.PluginUnloadEvent
import catmoe.fallencrystal.moefilter.api.logger.InitLogger
import catmoe.fallencrystal.moefilter.api.logger.LoggerManager
import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.api.user.displaycache.DisplayCache
import catmoe.fallencrystal.moefilter.common.config.ReloadConfig
import catmoe.fallencrystal.moefilter.common.utils.system.CPUMonitor
import catmoe.fallencrystal.moefilter.common.whitelist.WhitelistListener
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import catmoe.fallencrystal.moefilter.util.plugin.LoadCommand
import catmoe.fallencrystal.moefilter.util.plugin.luckperms.LuckPermsRegister
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

class MoeFilter : Plugin() {

    private val proxy = ProxyServer.getInstance()
    private val initLogger = InitLogger()

    override fun onEnable() {
        FilterPlugin.setPlugin(this)
        FilterPlugin.setDataFolder(dataFolder)

        EventManager // 初始化
        registerListener()

        LoadCommand(this).load()
        registerLogger()

        DisplayCache
        ProxyCache
        CPUMonitor
        initLogger.onLoad()
    }

    override fun onDisable() { EventManager.triggerEvent(PluginUnloadEvent()); initLogger.onUnload() }

    private fun registerLogger() {
        InitLogger()
        LoggerManager.registerFilter(LogHandler())
    }

    private fun registerListener() {
        EventManager.registerListener(ReloadConfig())
        EventManager.registerListener(WhitelistListener())
        EventManager.triggerEvent(PluginReloadEvent(null))
        val luckPermsRegister = LuckPermsRegister()
        luckPermsRegister.register()
    }
}