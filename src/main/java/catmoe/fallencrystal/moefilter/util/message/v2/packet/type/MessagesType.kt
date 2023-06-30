package catmoe.fallencrystal.moefilter.util.message.v2.packet.type

enum class MessagesType(@JvmField val prefix: String) {
    ACTION_BAR("actionbar @"),
    CHAT("chat @")
}