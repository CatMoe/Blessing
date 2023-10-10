/*
 * Copyright 2023. CatMoe / FallenCrystal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package catmoe.fallencrystal.moefilter.network.common

import catmoe.fallencrystal.moefilter.common.counter.ConnectionStatistics
import catmoe.fallencrystal.moefilter.common.counter.type.BlockType
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.common.firewall.Throttler
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidHandshakeException
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidPacketException
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidStatusPingException
import catmoe.fallencrystal.moefilter.network.limbo.packet.exception.InvalidVarIntException
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
