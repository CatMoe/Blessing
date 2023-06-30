package catmoe.fallencrystal.moefilter.util.message.v2.packet

import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType

interface MessagePacket {
    fun getType(): MessagesType

    fun supportChecker(version: Int): Boolean
}