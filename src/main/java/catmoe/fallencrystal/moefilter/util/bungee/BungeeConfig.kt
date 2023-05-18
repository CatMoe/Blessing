package catmoe.fallencrystal.moefilter.util.bungee

import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File

object BungeeConfig {
    private val bungeeFolder = FilterPlugin.getDataFolder()!!.parentFile.parentFile
    private val configFile = File(bungeeFolder, "config.yml")
    private val config = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(configFile)

    fun getString(path: String): String { return config.getString(path) }
    fun getInt(path: String): Int { return config.getInt(path) }
    fun getBoolean(path: String): Boolean { return config.getBoolean(path) }
}