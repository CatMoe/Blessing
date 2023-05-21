package catmoe.fallencrystal.moefilter

import catmoe.fallencrystal.moefilter.api.command.impl.test.log.LogHandler
import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.PluginReloadEvent
import catmoe.fallencrystal.moefilter.api.logger.LoggerManager
import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.api.user.displaycache.DisplayCache
import catmoe.fallencrystal.moefilter.common.config.ReloadConfig
import catmoe.fallencrystal.moefilter.common.whitelist.WhitelistListener
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import catmoe.fallencrystal.moefilter.util.plugin.LoadCommand
import catmoe.fallencrystal.moefilter.util.plugin.luckperms.LuckPermsListener
import catmoe.fallencrystal.moefilter.util.system.CPUMonitor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

class MoeFilter : Plugin() {

    private val proxy = ProxyServer.getInstance()

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
    }

    override fun onDisable() {
    }

    private fun registerLogger() {
        proxy.logger.filter = LoggerManager
        LoggerManager.registerFilter(LogHandler())
    }

    private fun registerListener() {
        EventManager.registerListener(ReloadConfig())
        EventManager.registerListener(WhitelistListener())
        EventManager.registerListener(ReloadConfig())
        EventManager.triggerEvent(PluginReloadEvent(null))
    }

    private fun registerLuckPermsListener() { if (proxy.pluginManager.getPlugin("LuckPerms") != null) { LuckPermsListener } }

}