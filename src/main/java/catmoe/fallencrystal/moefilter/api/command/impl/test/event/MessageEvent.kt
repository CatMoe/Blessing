package catmoe.fallencrystal.moefilter.api.command.impl.test.event

import catmoe.fallencrystal.moefilter.api.event.EventListener
import catmoe.fallencrystal.moefilter.api.event.FilterEvent
import catmoe.fallencrystal.moefilter.api.event.events.TestMessageEvent
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import net.md_5.bungee.api.connection.ProxiedPlayer

class MessageEvent : EventListener {

    @FilterEvent
    fun handleMessageEvent(event: TestMessageEvent) {
        val sender = event.sender
        val message = event.message
        val type = event.type
        if (sender !is ProxiedPlayer) { MessageUtil.logInfo(message); return } // Console
        MessageUtil.sendMessage(sender, type, message)
    }
}