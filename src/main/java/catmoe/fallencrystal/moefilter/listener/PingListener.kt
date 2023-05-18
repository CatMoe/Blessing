package catmoe.fallencrystal.moefilter.listener

import catmoe.fallencrystal.moefilter.common.blacklist.BlacklistObject
import catmoe.fallencrystal.moefilter.common.check.joinping.JoinPingChecks
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import net.md_5.bungee.api.ServerPing
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.ProxyPingEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.net.InetSocketAddress

class PingListener : Listener {
    @Suppress("DEPRECATION")
    @EventHandler
    fun onPing(event: ProxyPingEvent) {
        val address = (event.connection.socketAddress as InetSocketAddress).address.hostAddress
        val blacklist = BlacklistObject.getBlacklist(address)
        if (blacklist != null) {
            val ping = event.response
            val protocol = ServerPing.Protocol("Blacklisted", 1)

            val playerList = listOf(
                "&cYou are blacklisted on this server!",
                "&cReason: ${blacklist.reason}"
            )
            val players = ServerPing.Players(0, 0, playerList.map { name -> ServerPing.PlayerInfo(name, "") }.toTypedArray() )
            ping.favicon = ""
            ping.version = protocol
            ping.players = players
            ping.descriptionComponent = TextComponent(MessageUtil.colorize("You are blacklisted on this server. \n${blacklist.reason}"))
            event.response = ping
        }
        JoinPingChecks.onPing(address)
    }
}