package catmoe.fallencrystal.moefilter.util.plugin

import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.PluginReloadEvent
import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.api.user.displaycache.DisplayCache
import catmoe.fallencrystal.moefilter.common.config.ReloadConfig
import catmoe.fallencrystal.moefilter.common.utils.system.CPUMonitor
import catmoe.fallencrystal.moefilter.common.whitelist.WhitelistListener
import catmoe.fallencrystal.moefilter.util.plugin.luckperms.LuckPermsRegister
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

class AsyncLoader(val plugin: Plugin) {
    private val proxy = ProxyServer.getInstance()

    init {
        proxy.scheduler.runAsync(plugin) {
            FilterPlugin.setPlugin(plugin)

            EventManager // 初始化
            registerListener()

            LoadCommand(plugin).load()

            DisplayCache
            ProxyCache
            CPUMonitor
        }
    }

    private fun registerListener() {
        EventManager.registerListener(ReloadConfig())
        EventManager.registerListener(WhitelistListener())
        EventManager.triggerEvent(PluginReloadEvent(null))
        val luckPermsRegister = LuckPermsRegister()
        luckPermsRegister.register()
    }
}