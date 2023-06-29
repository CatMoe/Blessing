package catmoe.fallencrystal.moefilter.network.bungee.util.bconnection

import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.network.bungee.util.PipelineUtil
import io.netty.channel.ChannelPipeline
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.connection.PendingConnection
import net.md_5.bungee.connection.InitialHandler
import net.md_5.bungee.netty.ChannelWrapper
import net.md_5.bungee.protocol.DefinedPacket
import java.lang.reflect.Field
import java.net.InetAddress
import java.net.InetSocketAddress

@Suppress("UNUSED")
class ConnectionUtil(val connection: PendingConnection) {
    private val bungee = BungeeCord.getInstance()

    private val pipeline: ChannelPipeline? = (try { PipelineUtil.getChannelHandler(bungee.getPlayer(connection.uniqueId))?.pipeline() } catch (npe: NullPointerException) { null } ) ?: initChannelWrapper()?.handle?.pipeline()

    fun from(): String { return connection.virtualHost.hostString }

    fun getVersion(): Int { return connection.version }

    fun isConnected(): Boolean { return connection.isConnected }

    fun inetAddress(): InetAddress { return (connection.socketAddress as InetSocketAddress).address }

    fun close() { pipeline?.close() ?: connection.disconnect() }

    fun writePacket(packet: DefinedPacket) { connection.unsafe().sendPacket(packet) }

    fun writePacket(packet: Any) { pipeline!!.write(packet) }

    fun getPipeline(): ChannelPipeline? { return pipeline }

    private fun initChannelWrapper(): ChannelWrapper? {
        val initialHandler = connection as InitialHandler
        var field: Field? = null
        val debug = LocalConfig.getConfig().getBoolean("debug")
        return try {
            field = initialHandler.javaClass.getDeclaredField("ch")
            field!!.isAccessible=true
            field[initialHandler] as ChannelWrapper
        } catch (exception: Exception) { if (debug) exception.printStackTrace(); null
        } finally { if (field != null) { field.isAccessible=false } }
    }
}