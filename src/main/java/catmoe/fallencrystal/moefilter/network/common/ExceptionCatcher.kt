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

import catmoe.fallencrystal.moefilter.common.config.LocalConfig
import catmoe.fallencrystal.moefilter.common.counter.ConnectionCounter
import catmoe.fallencrystal.moefilter.common.counter.type.BlockType
import catmoe.fallencrystal.moefilter.common.firewall.Firewall
import catmoe.fallencrystal.moefilter.network.common.exception.DebugException
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidHandshakeStatusException
import catmoe.fallencrystal.moefilter.network.common.exception.InvalidStatusPingException
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import com.typesafe.config.ConfigException
import io.netty.channel.Channel
import java.io.IOException
import java.net.InetSocketAddress

object ExceptionCatcher {
    private var debug = false
    @JvmStatic
    fun handle(channel: Channel, cause: Throwable) {
        channel.close()
        if (debug) { cause.printStackTrace() }
        val address = (channel.remoteAddress() as InetSocketAddress).address
        if (cause is IOException) return
        if (cause is DebugException) { cause.printStackTrace(); return }
        if (cause is InvalidStatusPingException || cause is InvalidHandshakeStatusException) { Firewall.addAddress(address); return }
        if (cause is ConfigException) { MessageUtil.logError("<red>A connection forced closed because your config has critical issue"); cause.printStackTrace(); return }
        Firewall.addAddressTemp(address)
        ConnectionCounter.countBlocked(BlockType.FIREWALL)
    }

    fun reload() { debug = LocalConfig.getConfig().getBoolean("debug") }
}