package catmoe.fallencrystal.moefilter

import catmoe.fallencrystal.moefilter.api.logger.InitLogger
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.AsyncLoader
import com.typesafe.config.ConfigFactory
import net.md_5.bungee.api.plugin.Plugin
import java.io.File

class MoeFilter : Plugin() {

    private val initLogger = InitLogger()
    private val fastboot = try { ConfigFactory.parseFile(File(dataFolder, "config.conf")).getBoolean("fastboot") } catch (ex: Exception) { false }

    init { instance=this }

    override fun onEnable() { if(!fastboot) { load() } }

    override fun onDisable() {
        initLogger.onUnload()
        AsyncLoader.instance.unload()
    }

    private fun load() {
        val loader = AsyncLoader(this)
        initLogger.onLoad()
        loader.load()
    }

    override fun onLoad() {
        if (fastboot) { load() }
        MessageUtil.logInfo("[MoeFilter] Using MoeFilter API")
    }

    companion object {
        lateinit var instance: MoeFilter
            private set
    }
}