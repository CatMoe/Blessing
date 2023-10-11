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

package catmoe.fallencrystal.moefilter.util.message.v2.processor.cache

import catmoe.fallencrystal.moefilter.util.message.v2.packet.MessagePacket
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.message.v2.processor.IMessagePacketProcessor
import catmoe.fallencrystal.moefilter.util.message.v2.processor.PacketMessageType
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit

object MessagePacketCache {
    private val cache = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build<String, MessagePacket>()

    fun writePacket(processor: IMessagePacketProcessor, packet: MessagePacket) { cache.put("${getType(processor).prefix}${packet.getOriginal()}", packet) }

    fun readPacket(processor: IMessagePacketProcessor, message: String): MessagePacket? { return cache.getIfPresent("${getType(processor).prefix}$message") }

    private fun getType(processor: IMessagePacketProcessor): MessagesType { return processor::class.java.getAnnotation(PacketMessageType::class.java).type }
}