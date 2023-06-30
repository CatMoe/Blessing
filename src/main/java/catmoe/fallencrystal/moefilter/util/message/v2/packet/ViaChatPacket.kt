package catmoe.fallencrystal.moefilter.util.message.v2.packet

import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.protocol.ProtocolConstants
import net.md_5.bungee.protocol.packet.Chat
import net.md_5.bungee.protocol.packet.SystemChat

class ViaChatPacket(
    val v119: SystemChat?,
    val legacy: Chat?,
    val has119Data: Boolean,
    val hasLegacyData: Boolean,
    val bc: BaseComponent,
    val cs: String,
    val original: String
) : MessagePacket {
    override fun getType(): MessagesType { return MessagesType.CHAT }

    override fun supportChecker(version: Int): Boolean {
        if (has119Data && version >= ProtocolConstants.MINECRAFT_1_19) return true
        return hasLegacyData && version > ProtocolConstants.MINECRAFT_1_8
    }
}