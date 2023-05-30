package catmoe.fallencrystal.moefilter.api.command.impl.test.log

import catmoe.fallencrystal.moefilter.api.event.EventListener
import catmoe.fallencrystal.moefilter.api.event.FilterEvent
import catmoe.fallencrystal.moefilter.api.event.events.PluginUnloadEvent

class LogSpyUnload : EventListener {
    @FilterEvent
    fun unload(event: PluginUnloadEvent) { LogBroadcast.invalidateAll() }
}