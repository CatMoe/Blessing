package catmoe.fallencrystal.moefilter.api.command

import catmoe.fallencrystal.moefilter.api.command.impl.HelpCommand
import catmoe.fallencrystal.moefilter.api.command.impl.log.LogCommand
import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

class LoadCommand(private val plugin: Plugin) {

    val proxy = ProxyServer.getInstance().pluginManager

    fun load(){
        proxy.registerCommand(plugin, Command("moefilter", "", "ab", "antibot", "filter", "moefilter", "mf"))
        OCommand.register(HelpCommand())
    }

    fun debugCommand() {
        // 如果并未启用
        if (!ObjectConfig.getConfig().getBoolean("debug")) return
        OCommand.register(LogCommand())
    }
}