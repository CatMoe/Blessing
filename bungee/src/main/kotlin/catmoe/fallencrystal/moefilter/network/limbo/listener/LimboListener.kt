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

package catmoe.fallencrystal.moefilter.network.limbo.listener

import catmoe.fallencrystal.moefilter.network.limbo.handler.LimboHandler
import catmoe.fallencrystal.moefilter.network.limbo.packet.LimboPacket
import catmoe.fallencrystal.translation.utils.config.Reloadable
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("unused")
object LimboListener : Reloadable {

    val listener = Caffeine.newBuilder()
        .build<Class<out LimboPacket>, MutableCollection<ILimboListener>>()
    private val listeners: MutableCollection<ILimboListener> = CopyOnWriteArrayList()

    fun register(clazz: ILimboListener) {
        val packets = clazz::class.java.getAnnotation(ListenPacket::class.java).packets.toList()
        packets.forEach {
            val o = listener.getIfPresent(it.java)
            if (o == null) { listener.put(it.java, mutableListOf(clazz)) }
            else {
                var conflict: ILimboListener? = null
                o.forEach { c -> if (c::class.java == clazz::class.java) conflict=c }
                if (conflict != null) o.remove(conflict)
                o.add(clazz)
                listener.put(it.java, o)
            }
        }
        listeners.add(clazz)
        clazz.register()
    }

    override fun reload() { for (c in listeners) { if (c is Reloadable) c.reload() else continue } }

    fun unregister(clazz: ILimboListener) {
        val packets = clazz::class.java.getAnnotation(ListenPacket::class.java).packets.toList()
        packets.forEach {
            val o = listener.getIfPresent(it.java) ?: return
            var t: ILimboListener? = null
            o.forEach { i -> if (i::class.java == clazz::class.java) t=i }
            o.remove(t ?: return)
            listener.put(it.java, o)
        }
        listeners.remove(clazz)
        clazz.unregister()
    }

    fun handleReceived(packet: LimboPacket, handler: LimboHandler?): Boolean {
        val isCancelled = AtomicBoolean(false)
        val listeners = this.listener.getIfPresent(packet::class.java) ?: return isCancelled.get()
        listeners.forEach { if (it.received(packet, handler ?: return isCancelled.get(), isCancelled.get())) isCancelled.set(true) }
        return isCancelled.get()
    }

    // return true = cancel sending
    fun handleSend(packet: LimboPacket, handler: LimboHandler?): Boolean {
        val cancelled = AtomicBoolean(false)
        val listeners = this.listener.getIfPresent(packet::class.java) ?: return false
        listeners.forEach { if (it.send(packet, handler ?: return false, cancelled.get())) cancelled.set(true) }
        return cancelled.get()
    }

}