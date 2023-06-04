package catmoe.fallencrystal.moefilter.util.plugin

import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.PluginReloadEvent
import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.api.user.displaycache.DisplayCache
import catmoe.fallencrystal.moefilter.common.config.LoadConfig
import catmoe.fallencrystal.moefilter.common.config.ReloadConfig
import catmoe.fallencrystal.moefilter.common.utils.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.utils.counter.SessionCounterListener
import catmoe.fallencrystal.moefilter.common.utils.system.CPUMonitor
import catmoe.fallencrystal.moefilter.common.whitelist.WhitelistListener
import catmoe.fallencrystal.moefilter.listener.firewall.listener.common.IncomingListener
import catmoe.fallencrystal.moefilter.util.bungee.BungeeEvent
import catmoe.fallencrystal.moefilter.util.plugin.luckperms.LuckPermsRegister
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

class AsyncLoader(val plugin: Plugin) {
    private val proxy = ProxyServer.getInstance()
    private val pluginManager = proxy.pluginManager

    init {
        proxy.scheduler.runAsync(plugin) {
            FilterPlugin.setPlugin(plugin)
            LoadConfig.loadConfig()

            EventManager // 初始化
            registerListener()

            LoadCommand(plugin).load()

            DisplayCache
            ProxyCache
            CPUMonitor
            ConnectionCounter
        }
    }

    private fun registerListener() {
        EventManager.registerListener(plugin, ReloadConfig())
        EventManager.registerListener(plugin, WhitelistListener())
        EventManager.triggerEvent(PluginReloadEvent(null))
        EventManager.registerListener(plugin, SessionCounterListener())
        registerLuckPermsListener()

        pluginManager.registerListener(plugin, IncomingListener())
        pluginManager.registerListener(plugin, BungeeEvent())
    }

    private fun registerLuckPermsListener() {
        if (proxy.pluginManager.getPlugin("LuckPerms") != null) {
            val luckPermsRegister = LuckPermsRegister()
            luckPermsRegister.register()
        }
    }
}