package catmoe.fallencrystal.moefilter.common.config

import catmoe.fallencrystal.moefilter.api.event.EventListener
import catmoe.fallencrystal.moefilter.api.event.FilterEvent
import catmoe.fallencrystal.moefilter.api.event.events.PluginReloadEvent
import catmoe.fallencrystal.moefilter.api.proxy.ProxyCache
import catmoe.fallencrystal.moefilter.common.check.mixed.MixedCheck
import catmoe.fallencrystal.moefilter.common.check.valid_name.ValidNameCheck
import catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher
import catmoe.fallencrystal.moefilter.network.bungee.util.kick.FastDisconnect
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.moefilter.util.message.notification.Notifications
import catmoe.fallencrystal.moefilter.util.plugin.LoadCommand

class ReloadConfig : EventListener {
    @FilterEvent
    fun reloadConfig(event: PluginReloadEvent) {
        // Executor is null == Starting plugin.
        // Load can hot load module without "if" syntax.
        val executor = event.executor
        if (executor != null) { LoadConfig.instance.loadConfig(); LocalConfig.reloadConfig(); LoadCommand().reload(); warnMessage(event) }
        else { LoadCommand().load(); ValidNameCheck }
        ProxyCache.reload()
        Notifications.reload()
        FastDisconnect.initMessages()
        ExceptionCatcher.reload()
        MixedCheck.reload()
        ValidNameCheck.instance.init()
    }

    private fun warnMessage(event: PluginReloadEvent) {
        val sender = event.executor ?: return
        val messageConfig = LocalConfig.getMessage()
        val message = "${messageConfig.getString("prefix")}${messageConfig.getString("reload-warn")}"
        MessageUtil.sendMessage(sender, MessageUtil.colorizeMiniMessage(message))
    }
}