package catmoe.fallencrystal.moefilter.util.message.v2.packet

import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.protocol.ProtocolConstants
import net.md_5.bungee.protocol.packet.Chat
import net.md_5.bungee.protocol.packet.SystemChat
import net.md_5.bungee.protocol.packet.Title

class ViaActionbarPacket(
    val v119: SystemChat?,
    val v117: Chat?,
    val v111: Title?,
    val v110: Chat?,
    val has119Data: Boolean,
    val has117Data: Boolean,
    val has111Data: Boolean,
    val has110Data: Boolean,
    val bc: BaseComponent,
    val cs: String,
    val originalMessage: String,
) : MessagePacket {
    override fun getType(): MessagesType { return MessagesType.ACTION_BAR }

    override fun supportChecker(version: Int): Boolean {
        if (has119Data && version >= ProtocolConstants.MINECRAFT_1_19) return true
        if (has117Data && version > ProtocolConstants.MINECRAFT_1_17) return true
        if (has111Data && version > ProtocolConstants.MINECRAFT_1_10) return true
        return has110Data && version > ProtocolConstants.MINECRAFT_1_8
    }

    override fun getBaseComponent(): BaseComponent { return bc }

    override fun getComponentSerializer(): String { return cs }

    override fun getOriginal(): String { return originalMessage }
}