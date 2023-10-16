package catmoe.fallencrystal.moefilter.util.message.v2.packet.type

import catmoe.fallencrystal.moefilter.util.message.v2.processor.IMessagePacketProcessor
import catmoe.fallencrystal.moefilter.util.message.v2.processor.actionbar.ActionbarPacketProcessor
import catmoe.fallencrystal.moefilter.util.message.v2.processor.chat.ChatPacketProcessor

enum class MessagesType(@JvmField val prefix: String, @JvmField val processor: IMessagePacketProcessor) {
    ACTION_BAR("actionbar @", ActionbarPacketProcessor()),
    CHAT("chat @", ChatPacketProcessor())
}