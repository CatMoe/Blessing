package catmoe.fallencrystal.moefilter.api.event.events

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.CommandSender

@Suppress("unused")
class TestMessageEvent(val sender: CommandSender, val type: ChatMessageType, val message: String)