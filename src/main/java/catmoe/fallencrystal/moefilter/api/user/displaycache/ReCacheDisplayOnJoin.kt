package catmoe.fallencrystal.moefilter.api.user.displaycache

import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class ReCacheDisplayOnJoin : Listener {
    @EventHandler(priority = 127)
    fun onUpdateDisplayOnJoin(event: PostLoginEvent) { DisplayCache.getDisplay(event.player.uniqueId) }
}