package catmoe.fallencrystal.moefilter.api.command.impl.test.event

import catmoe.fallencrystal.moefilter.api.event.EventListener
import catmoe.fallencrystal.moefilter.api.event.FilterEvent
import catmoe.fallencrystal.moefilter.api.event.events.TestMessageEvent
import catmoe.fallencrystal.moefilter.util.MessageUtil
import net.md_5.bungee.api.ChatMessageType.*
import net.md_5.bungee.api.connection.ProxiedPlayer

class MessageEvent : EventListener {
    @FilterEvent
    fun handleMessageEvent(event: TestMessageEvent) {
        val sender = event.sender
        val message = event.message
        val type = event.type
        if (sender !is ProxiedPlayer) { MessageUtil.logInfo(message); return } // Console
        when (type) {
            CHAT -> { MessageUtil.sendMessage(sender, message) }
            ACTION_BAR -> { MessageUtil.sendActionbar(sender, message) }
            SYSTEM -> { throw IllegalStateException("Unsupported type $type") }
        }
    }
}