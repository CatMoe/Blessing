package catmoe.fallencrystal.moefilter.util.plugin

import net.md_5.bungee.api.plugin.Plugin
import java.io.File

object FilterPlugin {
    private var filterPlugin: Plugin? = null
    private var dataFolder: File? = null
    private var isEnabled = false

    fun setPlugin(plugin: Plugin) { filterPlugin = plugin }

    fun getPlugin(): Plugin? { return filterPlugin }

    fun setDataFolder(folder: File) { dataFolder = folder }

    fun getDataFolder(): File? { return dataFolder }

    fun pluginEnabled(): Boolean {return isEnabled}
    fun setEnabled(b: Boolean) { isEnabled=b }
}