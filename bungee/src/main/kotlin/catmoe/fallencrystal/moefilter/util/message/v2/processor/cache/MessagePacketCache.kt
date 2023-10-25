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

package catmoe.fallencrystal.moefilter.util.message.v2.processor.cache

import catmoe.fallencrystal.moefilter.util.message.v2.packet.MessagePacket
import catmoe.fallencrystal.moefilter.util.message.v2.packet.type.MessagesType
import catmoe.fallencrystal.moefilter.util.message.v2.processor.IMessagePacketProcessor
import catmoe.fallencrystal.moefilter.util.message.v2.processor.PacketMessageType
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit


@Suppress("MemberVisibilityCanBePrivate")
class MessagePacketCache(val processor: IMessagePacketProcessor) {

    fun writeCache(packet: MessagePacket) { cache.put(getContext(packet.getOriginal()), packet) }

    @Deprecated("Use readCachedAndWrite method.")
    fun getCachedPacket(message: String): MessagePacket? { return cache.getIfPresent(getContext(message)) }

    fun getType(): MessagesType { return processor::class.java.getAnnotation(PacketMessageType::class.java).type }

    @Deprecated("It will be auto expire in cache.")
    fun invalidateCache(message: String) { cache.invalidate(getContext(message)) }

    fun readCachedAndWrite(message: String): MessagePacket? {
        val point = "${getType().prefix}$message"
        val context = cache.getIfPresent(point)
        if (context != null) cache.put(point, context)
        return context
    }

    fun getContext(message: String): String {
        return "${getType().prefix}$message"
    }

    companion object {
        private val cache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build<String, MessagePacket>()
    }
}