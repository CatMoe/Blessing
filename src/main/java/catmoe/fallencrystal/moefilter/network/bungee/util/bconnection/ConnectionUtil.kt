package catmoe.fallencrystal.moefilter.network.bungee.util.bconnection

import net.md_5.bungee.api.connection.PendingConnection
import net.md_5.bungee.protocol.DefinedPacket
import java.net.InetAddress
import java.net.InetSocketAddress

class ConnectionUtil(val connection: PendingConnection) {
    fun write(packet: DefinedPacket) { connection.unsafe().sendPacket(packet) }

    fun from(): String { return connection.virtualHost.hostString }

    fun close() { connection.disconnect() }

    fun isConnected(): Boolean { return connection.isConnected }

    fun inetAddress(): InetAddress { return (connection.socketAddress as InetSocketAddress).address }
}