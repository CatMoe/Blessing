package catmoe.fallencrystal.moefilter.api.command.impl.test.log

import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.event.events.TestMessageEvent
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.CommandSender
import java.util.logging.LogRecord

object LogBroadcast {
    private val senders = mutableListOf<CommandSender>()

    fun addPlayer(sender: CommandSender) { senders.add(sender) }

    fun removePlayer(player: CommandSender) { senders.remove(player) }

    fun isInList(player: CommandSender): Boolean { return senders.contains(player) }

    fun broadcast(log: LogRecord) { senders.forEach { EventManager.triggerEvent(TestMessageEvent(it, ChatMessageType.ACTION_BAR, log.message))} }
}