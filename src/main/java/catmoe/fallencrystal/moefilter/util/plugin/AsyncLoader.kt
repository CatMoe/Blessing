package catmoe.fallencrystal.moefilter.util.plugin

import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.PluginReloadEvent
import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.api.user.displaycache.DisplayCache
import catmoe.fallencrystal.moefilter.common.config.LoadConfig
import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import catmoe.fallencrystal.moefilter.common.config.ReloadConfig
import catmoe.fallencrystal.moefilter.common.utils.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.utils.counter.SessionCounterListener
import catmoe.fallencrystal.moefilter.common.utils.maxmind.DownloadDatabase
import catmoe.fallencrystal.moefilter.common.utils.maxmind.InquireCountry
import catmoe.fallencrystal.moefilter.common.utils.system.CPUMonitor
import catmoe.fallencrystal.moefilter.common.whitelist.WhitelistListener
import catmoe.fallencrystal.moefilter.listener.firewall.listener.common.IncomingListener
import catmoe.fallencrystal.moefilter.util.bungee.BungeeEvent
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.notification.Notifications
import catmoe.fallencrystal.moefilter.util.plugin.luckperms.LuckPermsRegister
import com.typesafe.config.ConfigException
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import java.nio.file.Paths

class AsyncLoader(val plugin: Plugin, val utilMode: Boolean) {
    private val proxy = ProxyServer.getInstance()
    private val pluginManager = proxy.pluginManager

    private val folder = FilterPlugin.getDataFolder()!!

    private val configIssue = listOf(
        "----------  PLEASE DON'T REPORT THIS ISSUE TO CATMOE! ----------",
        "",
        "  Your config file is broken or not updated. -- You using snapshot build?",
        "  Please remove your config then restart proxy.",
        "  Make sure you using latest snapshot build",
        "",
        "  You can look exception message to find where",
        "  configured incorrectly or missing something",
        "",
        "  Proxy won't start until you fix that issue.",
        "  If you don't know anything. Then backup your config is best option.",
        "",
        "----------  PLEASE DON'T REPORT THIS ISSUE TO CATMOE! ----------",
    )

    init {
        proxy.scheduler.runAsync(plugin) {
            try {
                FilterPlugin.setPlugin(plugin)
                LoadConfig.loadConfig()

                EventManager // 初始化

                // check they init method to get more information
                DisplayCache
                ProxyCache
                CPUMonitor
                if (!utilMode) {
                    registerListener()
                    LoadCommand(plugin).load()
                    ConnectionCounter
                    Notifications
                    if (ObjectConfig.getProxy().getBoolean("country.enabled")) { loadMaxmindDatabase() }
                }
            } catch (configException: ConfigException) {
                configIssue.forEach { MessageUtil.logError(it) }
                configException.localizedMessage
                configException.printStackTrace()
                proxy.stop()
            }
        }
    }

    private fun loadMaxmindDatabase() {
        val maxmindLicense = try { ObjectConfig.getProxy().getString("country.key") } catch (_: Exception) { null }
        if (maxmindLicense.isNullOrEmpty()) return
        if (!Paths.get("${folder.absolutePath}/geolite/GeoLite2-Country.mmdb").toFile().exists()) { DownloadDatabase(folder, maxmindLicense) }
        InquireCountry
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