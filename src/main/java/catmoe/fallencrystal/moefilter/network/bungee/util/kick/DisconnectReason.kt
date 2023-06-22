package catmoe.fallencrystal.moefilter.network.bungee.util.kick

import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.protocol.packet.Kick

class DisconnectReason(val type: DisconnectType, val bc: BaseComponent, val packet: Kick)