package catmoe.fallencrystal.moefilter.listener

import catmoe.fallencrystal.moefilter.common.blacklist.BlacklistObject
import catmoe.fallencrystal.moefilter.util.message.kick.KickType
import net.md_5.bungee.api.event.PreLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.net.InetSocketAddress

class PreJoinListener : Listener {
    @EventHandler
    fun onPreJoin(event: PreLoginEvent) {
        val address = (event.connection.socketAddress as InetSocketAddress).address.hostAddress
        val blacklist = BlacklistObject.getBlacklist(address)
        if (blacklist != null) { setCancelled(event, KickType.BLACKLISTED) }
    }

    private fun setCancelled(event: PreLoginEvent, reason: KickType) {
        TODO()
    }
}