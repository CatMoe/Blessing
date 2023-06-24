package catmoe.fallencrystal.moefilter.network.bungee.util.bconnection

import catmoe.fallencrystal.moefilter.common.config.ObjectConfig
import net.md_5.bungee.api.connection.PendingConnection
import net.md_5.bungee.connection.InitialHandler
import net.md_5.bungee.netty.ChannelWrapper
import net.md_5.bungee.protocol.DefinedPacket
import java.lang.reflect.Field
import java.net.InetAddress
import java.net.InetSocketAddress

class ConnectionUtil(val connection: PendingConnection) {
    fun write(packet: DefinedPacket) { connection.unsafe().sendPacket(packet) }

    fun from(): String { return connection.virtualHost.hostString }

    fun close() { connection.disconnect() }

    fun isConnected(): Boolean { return connection.isConnected }

    fun inetAddress(): InetAddress { return (connection.socketAddress as InetSocketAddress).address }

    fun getChannelWrapper(): ChannelWrapper? {
        val initialHandler = connection as InitialHandler
        var field: Field? = null
        val debug = ObjectConfig.getConfig().getBoolean("debug")
        return try {
            field = initialHandler.javaClass.getDeclaredField("ch")
            field!!.isAccessible=true
            field.get(initialHandler) as ChannelWrapper
        } catch (exception: Exception) { if (debug) exception.printStackTrace(); null
        } finally { if (field != null) { field.isAccessible=false } }
    }
}