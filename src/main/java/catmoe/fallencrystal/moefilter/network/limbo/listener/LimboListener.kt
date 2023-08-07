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

package catmoe.fallencrystal.moefilter.network.limbo.listener

import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.atomic.AtomicBoolean

object LimboListener {

    val listener = Caffeine.newBuilder()
        .build<Class<out LimboPacket>, MutableCollection<ILimboListener>>()

    fun register(clazz: ILimboListener) {
        val packets = clazz::class.java.getAnnotation(HandlePacket::class.java).packets.toList()
        packets.forEach {
            val o = listener.getIfPresent(it.java)
            if (o == null) { listener.put(it.java, mutableListOf(clazz)) }
            else {
                var conflict: ILimboListener? = null; o.forEach { c -> if (c::class.java == clazz::class.java) conflict=c }
                if (conflict != null) o.remove(conflict); o.add(clazz)
                listener.put(it.java, o)
            }
        }
    }

    fun unregister(clazz: ILimboListener) {
        val packets = clazz::class.java.getAnnotation(HandlePacket::class.java).packets.toList()
        packets.forEach {
            val o = listener.getIfPresent(it.java) ?: return
            var t: ILimboListener? = null
            o.forEach { i -> if (i::class.java == clazz::class.java) t=i }
            o.remove(t ?: return)
            listener.put(it.java, o)
        }
    }

    fun handleReceived(packet: LimboPacket, handler: LimboHandler?): Boolean {
        val isCancelled = AtomicBoolean(false)
        val listeners = this.listener.getIfPresent(packet::class.java) ?: return isCancelled.get()
        listeners.forEach { if (it.received(packet, handler ?: return isCancelled.get(), isCancelled.get())) isCancelled.set(true) }
        return isCancelled.get()
    }

    // return true = cancel send
    fun handleSend(packet: LimboPacket, handler: LimboHandler?): Boolean {
        val cancelled = AtomicBoolean(false)
        val listeners = this.listener.getIfPresent(packet::class.java) ?: return false
        listeners.forEach { if (it.send(packet, handler ?: return false, cancelled.get())) cancelled.set(true) }
        return cancelled.get()
    }

}