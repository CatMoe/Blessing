package catmoe.fallencrystal.moefilter.util.plugin.luckperms

import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.luckperms.PermsUserDataRecalculateEvent
import catmoe.fallencrystal.moefilter.api.user.displaycache.ReCacheDisplayOnJoin
import catmoe.fallencrystal.moefilter.util.plugin.FilterPlugin
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.user.UserDataRecalculateEvent
import net.md_5.bungee.api.ProxyServer

object LuckPermsListener {
    init { registerEvent() }
    private fun registerEvent() {
        val luckperms = LuckPermsProvider.get()
        val plugin = FilterPlugin.getPlugin()

        registerDisplayCacheEvent()

        // private fun onUserDataRecalculateEvent -> UserDataRecalculateEvent
        luckperms.eventBus.subscribe(plugin!!, UserDataRecalculateEvent::class.java, LuckPermsListener::onUserDataRecalculateEvent)
    }

    private fun onUserDataRecalculateEvent(event: UserDataRecalculateEvent) { EventManager.triggerEvent(PermsUserDataRecalculateEvent(event.user, event.data, event.luckPerms, event.eventType)) }

    private fun registerDisplayCacheEvent() {
        val plugin = FilterPlugin.getPlugin()
        val proxy = ProxyServer.getInstance()
        proxy.pluginManager.registerListener(plugin, ReCacheDisplayOnJoin())
    }
}