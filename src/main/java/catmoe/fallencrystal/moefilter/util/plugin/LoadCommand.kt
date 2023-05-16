package catmoe.fallencrystal.moefilter.util.plugin

import catmoe.fallencrystal.moefilter.api.command.Command
import catmoe.fallencrystal.moefilter.api.command.OCommand
import catmoe.fallencrystal.moefilter.api.command.impl.HelpCommand
import catmoe.fallencrystal.moefilter.api.command.impl.test.event.MessageEvent
import catmoe.fallencrystal.moefilter.api.command.impl.test.event.TestEventCommand
import catmoe.fallencrystal.moefilter.api.command.impl.test.log.LogCommand
import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

class LoadCommand(private val plugin: Plugin) {

    val proxy = ProxyServer.getInstance().pluginManager

    fun load(){
        proxy.registerCommand(plugin, Command("moefilter", "", "ab", "antibot", "filter", "moefilter", "mf"))
        OCommand.register(HelpCommand())
        debugCommand()
    }

    private fun debugCommand() {
        // 如果并未启用
        if (!ObjectConfig.getConfig().getBoolean("debug")) return
        EventManager.registerListener(MessageEvent())
        OCommand.register(LogCommand())
        OCommand.register(TestEventCommand())
    }
}