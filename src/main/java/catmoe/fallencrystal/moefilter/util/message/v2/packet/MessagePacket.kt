package catmoe.fallencrystal.moefilter.util.message.v2.packet

import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import net.md_5.bungee.api.chat.BaseComponent

interface MessagePacket {
    fun getType(): MessagesType

    fun supportChecker(version: Int): Boolean

    fun getOriginal(): String

    fun getBaseComponent(): BaseComponent

    fun getComponentSerializer(): String
}