package catmoe.fallencrystal.moefilter

import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.PluginUnloadEvent
import catmoe.fallencrystal.moefilter.api.logger.InitLogger
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.AsyncLoader
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import com.typesafe.config.ConfigFactory
import net.md_5.bungee.api.plugin.Plugin
import java.io.File

class MoeFilter : Plugin() {

    private val initLogger = InitLogger()
    private val fastboot = try { ConfigFactory.parseFile(File(dataFolder, "proxy.conf")).getBoolean("fastboot") } catch (ex: Exception) { false }

    init { if (fastboot) { load() } }

    override fun onEnable() { if(!fastboot) { load() } }

    override fun onDisable() { EventManager.triggerEvent(PluginUnloadEvent()); initLogger.onUnload() }

    private fun load() {
        FilterPlugin.setEnabled(true)
        FilterPlugin.setPlugin(this)
        FilterPlugin.setDataFolder(dataFolder)
        initLogger.onLoad()
        AsyncLoader(this)
    }

    override fun onLoad() { MessageUtil.logInfo("[MoeFilter] Using MoeFilter API") }
}