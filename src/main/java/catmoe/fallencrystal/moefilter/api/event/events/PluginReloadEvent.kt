package catmoe.fallencrystal.moefilter.api.event.events

import catmoe.fallencrystal.moefilter.api.event.MoeEvent
import net.md_5.bungee.api.CommandSender

@Suppress("unused")
class PluginReloadEvent(val executor: CommandSender?) : MoeEvent
