/*
 * Copyright (C) 2023-2023. CatMoe / MoeFilter Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package catmoe.fallencrystal.moefilter.network.common

import catmoe.fallencrystal.moefilter.common.counter.ConnectionStatistics
import catmoe.fallencrystal.moefilter.data.BlockType
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.common.firewall.Throttler
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidHandshakeException
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidPacketException
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidStatusPingException
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidVarIntException
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import catmoe.fallencrystal.translation.utils.config.Reloadable
import com.typesafe.config.ConfigException
import io.netty.channel.Channel
import io.netty.handler.timeout.ReadTimeoutException
import java.io.IOException
import java.net.InetSocketAddress

object ExceptionCatcher : Reloadable {
    private var debug = false
    @JvmStatic
    fun handle(channel: Channel, cause: Throwable) {
        channel.close()
        if (debug) { cause.printStackTrace() }
        val address = (channel.remoteAddress() as InetSocketAddress).address
        when (cause) {
            is IOException -> return
            is InvalidVarIntException -> {
                Firewall.addAddress(address)
                ConnectionStatistics.countBlocked(BlockType.FIREWALL)
                return
            }
            is ReadTimeoutException -> {
                if (Throttler.isThrottled(address)) Firewall.addAddress(address)
                ConnectionStatistics.countBlocked(BlockType.TIMEOUT); return
            }
            is InvalidStatusPingException -> { Firewall.addAddress(address); return }
            is InvalidHandshakeException -> { Firewall.addAddress(address); return }
            is InvalidPacketException ->  { Firewall.addAddress(address); return }
            is ConfigException -> {
                MessageUtil.logError("<red>A connection forced closed because your config has critical issue")
                cause.printStackTrace(); return
            }
        }
        Firewall.addAddressTemp(address)
        ConnectionStatistics.countBlocked(BlockType.FIREWALL)
    }

    override fun reload() { debug = LocalConfig.getConfig().getBoolean("debug") }
}
