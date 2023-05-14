package catmoe.fallencrystal.moefilter.api.command.impl.log

import catmoe.fallencrystal.moefilter.util.MessageUtil
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.logging.LogRecord

object LogBroadcast {
    private val players = mutableListOf<ProxiedPlayer>()

    fun addPlayer(player: ProxiedPlayer) { players.add(player) }

    fun removePlayer(player: ProxiedPlayer) { players.remove(player) }

    fun isInList(player: ProxiedPlayer): Boolean { return players.contains(player) }

    fun broadcast(log: LogRecord) {
        players.forEach { MessageUtil.sendActionbar(it, "&e ${log.message}") }
    }
}