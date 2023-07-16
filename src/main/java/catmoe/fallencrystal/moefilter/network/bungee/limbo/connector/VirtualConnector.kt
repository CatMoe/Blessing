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

package catmoe.fallencrystal.moefilter.network.bungee.limbo.connector

import catmoe.fallencrystal.moefilter.network.bungee.limbo.dimension.Dimension
import catmoe.fallencrystal.moefilter.network.bungee.limbo.packets.util.UnsupportedPacketOperationException
import catmoe.fallencrystal.moefilter.network.bungee.util.ExceptionCatcher
import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import net.md_5.bungee.netty.PacketHandler
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("unused")
class VirtualConnector(private val connection: ConnectionUtil, private val dimension: Dimension) : PacketHandler() {

    private val channel = connection.getPipeline()!!.channel()
    private val name: String? = try { connection.connection.name } catch (_: Exception) { null }
    private var onGround: Boolean? = null
    private var x: Int? = null
    private var y: Int? = null
    private var z: Int? = null
    private val clientId = Random().nextInt( Integer.MAX_VALUE - 100 ) + 50
    private val disconnected = AtomicBoolean(!connection.isConnected())

    override fun toString(): String { throw UnsupportedPacketOperationException() }

    override fun exception(t: Throwable?) { ExceptionCatcher.handle(channel, t!!) }


}