/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package catmoe.fallencrystal.moefilter.util.plugin

import catmoe.fallencrystal.moefilter.MoeFilterBungee
import catmoe.fallencrystal.moefilter.api.command.CommandHandler
import catmoe.fallencrystal.moefilter.api.logger.BCLogType
import catmoe.fallencrystal.moefilter.api.logger.LoggerManager
import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.api.user.displaycache.DisplayCache
import catmoe.fallencrystal.moefilter.common.check.proxy.ProxyChecker
import catmoe.fallencrystal.moefilter.common.check.proxy.ipapi.IPAPIChecker
import catmoe.fallencrystal.moefilter.common.check.proxy.proxycheck.ProxyCheck
import catmoe.fallencrystal.moefilter.common.config.ReloadConfig
import catmoe.fallencrystal.moefilter.common.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.common.geoip.CountryMode
import catmoe.fallencrystal.moefilter.common.geoip.DownloadDatabase
import catmoe.fallencrystal.moefilter.common.geoip.GeoIPManager
import catmoe.fallencrystal.moefilter.common.state.AttackCounterListener
import catmoe.fallencrystal.moefilter.event.PluginReloadEvent
import catmoe.fallencrystal.moefilter.event.PluginUnloadEvent
import catmoe.fallencrystal.moefilter.listener.main.ExceptionFilter
import catmoe.fallencrystal.moefilter.listener.main.MainListener
import catmoe.fallencrystal.moefilter.network.InitChannel
import catmoe.fallencrystal.moefilter.network.bungee.util.WorkingMode
import catmoe.fallencrystal.moefilter.network.bungee.util.WorkingMode.*
import catmoe.fallencrystal.moefilter.network.limbo.handler.MoeLimbo
import catmoe.fallencrystal.moefilter.network.limbo.util.BungeeSwitcher
import catmoe.fallencrystal.moefilter.util.bungee.BungeeEvent
import catmoe.fallencrystal.moefilter.util.message.notification.Notifications
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.luckperms.LuckPermsRegister
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.CPlatform
import catmoe.fallencrystal.translation.event.EventListener
import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.event.annotations.EventHandler
import catmoe.fallencrystal.translation.event.annotations.HandlerPriority
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.system.CPUMonitor
import com.typesafe.config.ConfigException
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

@Suppress("SpellCheckingInspection", "MemberVisibilityCanBePrivate")
class AsyncLoader(val plugin: Plugin, val cLoader: CPlatform) : EventListener {
    private val proxy = ProxyServer.getInstance()
    private val pluginManager = proxy.pluginManager

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
                DisplayCache
                ProxyCache
                CPUMonitor.startSchedule()
                pluginManager.registerCommand(plugin, CommandHandler("moefilter", "", "ab", "antibot", "filter", "moefilter", "mf"))

                registerListener()
                ConnectionCounter
                Notifications
                if ( try { CountryMode.valueOf(LocalConfig.getProxy().getAnyRef("country.mode").toString()) != CountryMode.DISABLED } catch (_: Exception) { false } ) { loadMaxmindDatabase() }
                LoadCommand().load()
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
        when (mode) {
            PIPELINE -> { InitChannel().initPipeline() }
            EVENT -> {
                pluginManager.registerListener(plugin, MainListener.incomingListener)
                MessageUtil.logWarn("[MoeFilter] EVENT mode is deprecated. Don't expect strong protection!")
            }
            DISABLED -> { MessageUtil.logWarn("[MoeFilter] [Antibot] You choose to disabled antibot! If that not you want choose. Please select another mode in antibot.conf!") }
        }
        LoggerManager.registerFilter(ExceptionFilter())
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
            // if (!Paths.get("${folder.absolutePath}/geolite/GeoLite2-Country.mmdb").toFile().exists()) { DownloadDatabase(folder, maxmindLicense) }
            this.geoIPLoader=DownloadDatabase(folder)
        }
    }

    private fun registerListener() {
        /*
        EventManager.registerListener(plugin, ReloadConfig())
        EventManager.registerListener(plugin, AttackCounterListener())
        EventManager.triggerEvent(PluginReloadEvent(null))
         */
        EventManager.register(ReloadConfig)
        EventManager.register(AttackCounterListener())
        EventManager.callEvent(PluginReloadEvent(null))
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