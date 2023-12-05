/*
 * Copyright (C) 2023-2023. CatMoe / MoeFilter Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package catmoe.fallencrystal.moefilter.util.plugin

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.moefilter.api.command.CommandHandler
import catmoe.fallencrystal.moefilter.api.logger.BCLogType
import catmoe.fallencrystal.moefilter.api.logger.LoggerManager
import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.common.check.proxy.ProxyChecker
import catmoe.fallencrystal.moefilter.common.check.proxy.ipapi.IPAPIChecker
import catmoe.fallencrystal.moefilter.common.check.proxy.proxycheck.ProxyCheck
import catmoe.fallencrystal.moefilter.common.config.ReloadConfig
import catmoe.fallencrystal.moefilter.common.counter.ConnectionStatistics
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.common.geoip.CountryMode
import catmoe.fallencrystal.moefilter.common.geoip.DownloadDatabase
import catmoe.fallencrystal.moefilter.common.geoip.GeoIPManager
import catmoe.fallencrystal.moefilter.common.state.AttackCounterListener
import catmoe.fallencrystal.moefilter.event.PluginReloadEvent
import catmoe.fallencrystal.moefilter.event.PluginUnloadEvent
import catmoe.fallencrystal.moefilter.listener.BungeeEvent
import catmoe.fallencrystal.moefilter.listener.main.EventLoggerFilter
import catmoe.fallencrystal.moefilter.listener.main.MainListener
import catmoe.fallencrystal.moefilter.network.InitChannel
import catmoe.fallencrystal.moefilter.network.bungee.util.WorkingMode
import catmoe.fallencrystal.moefilter.network.bungee.util.WorkingMode.*
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.network.limbo.util.BungeeSwitcher
import catmoe.fallencrystal.moefilter.util.message.notification.Notifications
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.CPlatform
import catmoe.fallencrystal.translation.command.CommandAdapter
import catmoe.fallencrystal.translation.event.EventListener
import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.event.annotations.EventHandler
import catmoe.fallencrystal.translation.event.annotations.HandlerPriority
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.system.CPUMonitor
import com.typesafe.config.ConfigException
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.protocol.packet.Kick

@Suppress("SpellCheckingInspection", "MemberVisibilityCanBePrivate")
class AsyncLoader(val plugin: Plugin, val cLoader: CPlatform) : EventListener {
    private val proxy = ProxyServer.getInstance()
    private val pluginManager = proxy.pluginManager
    var mode: WorkingMode? = null
        private set

    var geoIPLoader: DownloadDatabase? = null

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
        isLegacy = try {
            // If successful. Indicates that the user is using an older version of BungeeCord.
            // See this issue on https://github.com/CatMoe/MoeFilter/issues/75
            Kick::class.java.getConstructor(String::class.java).newInstance("")
            true
        } catch (_: NoSuchMethodException) { false }
        try {
            cLoader.whenLoad()
            loadAntibot()
        } catch (ce: ConfigException) {
            configIssue.forEach { MessageUtil.logError(it) }
            ce.localizedMessage
            ce.printStackTrace()
            proxy.stop()
            return
        }
        scheduler.runAsync {
            EventManager.register(this)
            try {

                // check they init method to get more information
                ProxyCache
                CPUMonitor.startSchedule()
                //pluginManager.registerCommand(plugin, CommandHandler("moefilter", "", "ab", "antibot", "filter", "moefilter", "mf"))
                CommandAdapter.register(CommandHandler())

                registerListener()
                ConnectionStatistics
                Notifications
                if ( try { CountryMode.valueOf(LocalConfig.getProxy().getAnyRef("country.mode").toString()) != CountryMode.DISABLED } catch (_: Exception) { false } ) { loadMaxmindDatabase() }
                Firewall.load()
                loadProxyAPI()
                if (LocalConfig.getLimbo().getBoolean("enabled")) {
                    MoeLimbo.initLimbo()
                    // EventManager.registerListener(plugin, BungeeSwitcher)
                    EventManager.register(BungeeSwitcher)
                }
            } catch (configException: ConfigException) {
                configIssue.forEach { MessageUtil.logError(it) }
                configException.localizedMessage
                configException.printStackTrace()
                proxy.stop()
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @EventHandler(PluginUnloadEvent::class, priority = HandlerPriority.LOWEST)
    fun unload(event: PluginUnloadEvent) {
        CommandAdapter.unregister(CommandHandler.instance)
        CPUMonitor.shutdownSchedule()
        try {
            MessageUtil.logInfo("[MoeFilter] Waiting event calling")
        } catch (ex: Exception) {
            MessageUtil.logWarn("[MoeFilter] Exception occurred while thread waiting.")
            ex.printStackTrace()
        }
        Firewall.shutdown()
        for (listener in listOf(
            this,
            BungeeSwitcher,
            ReloadConfig,
            AttackCounterListener(),
        )) {
            EventManager.unregister(listener)
        }
        MessageUtil.logInfo("[MoeFilter] MoeFilter are unloaded.")
    }

    private fun loadAntibot() {
        val mode = try { WorkingMode.valueOf(LocalConfig.getAntibot().getAnyRef("mode").toString()) } catch (ignore: Exception) { PIPELINE }
        MainListener.incomingListener = if (LoggerManager.getType() == BCLogType.WATERFALL)
            catmoe.fallencrystal.moefilter.listener.listener.waterfall.IncomingListener()
        else catmoe.fallencrystal.moefilter.listener.listener.common.IncomingListener()
        this.mode=mode
        when (mode) {
            PIPELINE -> InitChannel().initPipeline()
            EVENT -> {
                pluginManager.registerListener(plugin, MainListener.incomingListener)
                LoggerManager.registerFilter(EventLoggerFilter())
                MessageUtil.logWarn("[MoeFilter] EVENT mode is deprecated. Don't expect strong protection!")
            }
            DISABLED -> { MessageUtil.logWarn("[MoeFilter] [Antibot] You choose to disabled antibot! If that not you want choose. Please select another mode in antibot.conf!") }
        }
        MoeFilterBungee.mode = mode
    }

    private fun loadProxyAPI() {
        val conf = LocalConfig.getProxy()
        if (conf.getBoolean("ip-api.enable")) ProxyChecker.addAPI(IPAPIChecker())
        if (conf.getBoolean("proxycheck-io.enable")) ProxyChecker.addAPI(ProxyCheck())
    }

    private fun loadMaxmindDatabase() {
        scheduler.runAsync {
            GeoIPManager
            val folder = MoeFilterBungee.instance.dataFolder
            val maxmindLicense = try { LocalConfig.getProxy().getString("country.key") } catch (_: Exception) { null }
            if (maxmindLicense.isNullOrEmpty()) { MessageUtil.logWarn("[MoeFilter] [GeoIP] Your maxmind license is empty. Country mode are disabled."); return@runAsync }
            this.geoIPLoader=DownloadDatabase(folder)
        }
    }

    private fun registerListener() {
        EventManager.register(ReloadConfig)
        EventManager.register(AttackCounterListener())
        EventManager.callEvent(PluginReloadEvent(null))

        pluginManager.registerListener(plugin, BungeeEvent())
    }

    companion object {
        lateinit var instance: AsyncLoader
            private set
        var isLegacy: Boolean = false
            private set
    }
}