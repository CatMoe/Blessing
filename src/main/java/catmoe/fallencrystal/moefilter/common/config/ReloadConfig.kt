package catmoe.fallencrystal.moefilter.common.config

import catmoe.fallencrystal.moefilter.api.event.EventListener
import catmoe.fallencrystal.moefilter.api.event.FilterEvent
import catmoe.fallencrystal.moefilter.api.event.events.PluginReloadEvent
import catmoe.fallencrystal.moefilter.util.message.MessageUtil

class ReloadConfig : EventListener {
    @FilterEvent
    fun reloadConfig(event: PluginReloadEvent) { LoadConfig.loadConfig(); warnMessage(event) }

    private fun warnMessage(event: PluginReloadEvent) {
        val sender = event.executor
        val messageConfig = ObjectConfig.getMessage()
        val message = "${messageConfig.getString("prefix")}${messageConfig.getConfig("reload-warn")}"
        MessageUtil.sendMessage(sender!!, message)
    }
}