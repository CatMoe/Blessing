package catmoe.fallencrystal.moefilter.util.plugin

import catmoe.fallencrystal.moefilter.MoeFilter
import catmoe.fallencrystal.moefilter.api.command.CommandHandler
import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.PluginReloadEvent
import catmoe.fallencrystal.moefilter.api.event.events.PluginUnloadEvent
import catmoe.fallencrystal.moefilter.api.logger.BCLogType
import catmoe.fallencrystal.moefilter.api.logger.LoggerManager
import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.api.user.displaycache.DisplayCache
import catmoe.fallencrystal.moefilter.common.config.LoadConfig
import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.config.ReloadConfig
import catmoe.fallencrystal.moefilter.common.utils.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.utils.counter.SessionCounterListener
import catmoe.fallencrystal.moefilter.common.utils.maxmind.CountryMode
import catmoe.fallencrystal.moefilter.common.utils.maxmind.DownloadDatabase
import catmoe.fallencrystal.moefilter.common.utils.maxmind.InquireCountry
import catmoe.fallencrystal.moefilter.common.utils.system.CPUMonitor
import catmoe.fallencrystal.moefilter.common.whitelist.WhitelistListener
import catmoe.fallencrystal.moefilter.listener.main.ExceptionFilter
import catmoe.fallencrystal.moefilter.network.InitChannel
import catmoe.fallencrystal.moefilter.network.bungee.util.WorkingMode
import catmoe.fallencrystal.moefilter.network.bungee.util.WorkingMode.*
import catmoe.fallencrystal.moefilter.util.bungee.BungeeEvent
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.notification.Notifications
import catmoe.fallencrystal.moefilter.util.plugin.luckperms.LuckPermsRegister
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import com.typesafe.config.ConfigException
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import java.nio.file.Paths

class AsyncLoader(val plugin: Plugin) {
    private val proxy = ProxyServer.getInstance()
    private val pluginManager = proxy.pluginManager

    private val configLoader = LoadConfig()

    private val scheduler = Scheduler(plugin)

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

    init { instance=this }


    fun load() {
        scheduler.runAsync {
            try {
                configLoader.loadConfig()

                EventManager // 初始化

                // check they init method to get more information
                DisplayCache
                ProxyCache
                CPUMonitor.startSchedule()
                pluginManager.registerCommand(plugin, CommandHandler("moefilter", "", "ab", "antibot", "filter", "moefilter", "mf"))

                registerListener()
                ConnectionCounter
                Notifications
                if ( try { CountryMode.valueOf(LocalConfig.getProxy().getAnyRef("country.mode").toString()) != CountryMode.DISABLED } catch (_: Exception) { false } ) { loadMaxmindDatabase() }
                loadAntibot()
            } catch (configException: ConfigException) {
                configIssue.forEach { MessageUtil.logError(it) }
                configException.localizedMessage
                configException.printStackTrace()
                proxy.stop()
            }
        }
    }

    fun unload() {
        EventManager.triggerEvent(PluginUnloadEvent())
        CPUMonitor.shutdownSchedule()
        try {
            MessageUtil.logInfo("[MoeFilter] Waiting event calling")
            Thread.sleep(1000)
        } catch (ex: Exception) {
            MessageUtil.logWarn("[MoeFilter] Exception occurred while thread waiting.")
            ex.printStackTrace()
        }
        MessageUtil.logInfo("[MoeFilter] MoeFilter are unloaded.")
    }

    private fun loadAntibot() {
        when (try { WorkingMode.valueOf(LocalConfig.getAntibot().getAnyRef("mode").toString()) } catch (ignore: Exception) { PIPELINE }) {
            PIPELINE -> { InitChannel().initPipeline() }
            EVENT -> {
                val waterfallListener = catmoe.fallencrystal.moefilter.listener.firewall.listener.waterfall.IncomingListener()
                val commonListener = catmoe.fallencrystal.moefilter.listener.firewall.listener.common.IncomingListener()
                val choose = if (LoggerManager.getType() == BCLogType.WATERFALL) waterfallListener else commonListener
                pluginManager.registerListener(plugin, choose)
                LoggerManager.registerFilter(ExceptionFilter())
            }
            DISABLED -> { MessageUtil.logWarn("[MoeFilter] [Antibot] You choose to disabled antibot! If that not you want choose. Please select another mode in antibot.conf!") }
        }
    }

    private fun loadMaxmindDatabase() {
        scheduler.runAsync {
            val folder = MoeFilter.instance.dataFolder
            val maxmindLicense = try { LocalConfig.getProxy().getString("country.key") } catch (_: Exception) { null }
            if (maxmindLicense.isNullOrEmpty()) { MessageUtil.logWarn("[MoeFilter] [GeoIP] Your maxmind license is empty. Country mode are disabled."); return@runAsync }
            if (!Paths.get("${folder.absolutePath}/geolite/GeoLite2-Country.mmdb").toFile().exists()) { DownloadDatabase(folder, maxmindLicense) }
            InquireCountry
        }
    }

    private fun registerListener() {
        EventManager.registerListener(plugin, ReloadConfig())
        EventManager.registerListener(plugin, WhitelistListener())
        EventManager.registerListener(plugin, SessionCounterListener())
        EventManager.triggerEvent(PluginReloadEvent(null))
        registerLuckPermsListener()

        /*
        if (LoggerManager.getType() == BCLogType.WATERFALL) pluginManager.registerListener(plugin, catmoe.fallencrystal.moefilter.listener.firewall.listener.waterfall.IncomingListener())
        else pluginManager.registerListener(plugin, catmoe.fallencrystal.moefilter.listener.firewall.listener.common.IncomingListener())
         */

        pluginManager.registerListener(plugin, BungeeEvent())
    }

    private fun registerLuckPermsListener() {
        if (proxy.pluginManager.getPlugin("LuckPerms") != null) {
            val luckPermsRegister = LuckPermsRegister()
            luckPermsRegister.register()
        }
    }

    companion object {
        lateinit var instance: AsyncLoader
            private set
    }
}