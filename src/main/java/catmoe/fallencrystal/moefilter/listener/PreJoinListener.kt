package catmoe.fallencrystal.moefilter.listener

import catmoe.fallencrystal.moefilter.common.blacklist.BlacklistObject
import catmoe.fallencrystal.moefilter.common.check.BlockedType.*
import catmoe.fallencrystal.moefilter.common.check.CheckManager
import catmoe.fallencrystal.moefilter.common.check.reason.CachedReason
import catmoe.fallencrystal.moefilter.common.check.reason.KickReason
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.PreLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.net.InetSocketAddress

class PreJoinListener : Listener {
    @EventHandler
    fun onPreJoin(event: PreLoginEvent) {
        val address = (event.connection.socketAddress as InetSocketAddress).address.hostAddress
        if (BlacklistObject.getBlacklist(address) != null) { cancelled(event, KickReason.Blacklisted); return }
        when (CheckManager.shouldBlocked(address)) {
            BLOCKED_FIRST_JOIN -> { cancelled(event, KickReason.FirstJoin); return; }
            BLOCKED_PING_JOIN -> { cancelled(event, KickReason.PingJoin); return }
            IGNORE -> {}
        }
    }

    private fun cancelled(event: PreLoginEvent, reason: KickReason) {
        val message = MessageUtil.colorize(CachedReason.getReason(reason)!!)
        event.isCancelled = true
        event.setCancelReason(TextComponent(message.joinToString("\n")))
    }
}