/*
 * Copyright (C) 2023-2023. CatMoe / Blessing Contributors
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

package net.miaomoe.blessing.event

import com.github.benmanes.caffeine.cache.Caffeine
import net.miaomoe.blessing.event.adapter.ListenerAdapter
import net.miaomoe.blessing.event.event.BlessingEvent
import net.miaomoe.blessing.event.event.Cancellable
import net.miaomoe.blessing.event.info.ListenerInfo
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

@Suppress("MemberVisibilityCanBePrivate")
object EventManager {

    private val cache = Caffeine
        .newBuilder()
        .build<KClass<out BlessingEvent>, Bundle>()

    internal data class Bundle(
        val event: KClass<out BlessingEvent>,
        val listener: MutableList<Listener> = mutableListOf()
    )

    internal data class Listener(
        val event: KClass<out BlessingEvent>,
        val info: ListenerInfo,
        val listener: ListenerAdapter
    )

    fun register(event: KClass<out BlessingEvent>, info: ListenerInfo, listener: ListenerAdapter) {
        val data = Listener(event, info, listener)
        val bundle = cache.getIfPresent(event) ?: Bundle(event)
        require(!bundle.listener.any { it.info == info }) { "Conflicted ListenerInfo!" }
        bundle.listener.add(data)
        cache.put(event, bundle)
        sortListener(event)
    }

    fun unregister(event: KClass<out BlessingEvent>, info: ListenerInfo) {
        val bundle = cache.getIfPresent(event) ?: return
        bundle.listener.firstOrNull { it.info == info }?.let { bundle.listener.remove(it) }
        sortListener(event)
    }

    fun unregister(event: KClass<out BlessingEvent>, key: Any) {
        val bundle = cache.getIfPresent(event) ?: return
        bundle.listener.filter { it.info.key == key }.forEach { unregister(event, it) }
    }

    fun sortListener(event: KClass<out BlessingEvent>) {
        val bundle = cache.getIfPresent(event) ?: return
        val shorted = bundle.listener.sortedByDescending { it.info.priority.ordinal }.toMutableList()
        bundle.listener.let {
            it.clear()
            it.addAll(shorted)
        }
    }

    fun call(event: BlessingEvent) {
        val bundle = cache.getIfPresent(event::class) ?: return
        val isCancelled = if (event is Cancellable) event.isCancelled else false
        for (listener in bundle.listener.toList()) {
            val info = listener.info
            if (isCancelled && !info.ignoreCancelled) continue
            fun invoke() {
                try {
                    listener.listener.invoke(event)
                } catch (exception: Exception) {
                    if (info.silentException) exception.printStackTrace() else throw exception
                }
            }
            if (info.async) CompletableFuture.runAsync { invoke() } else invoke()
        }
    }
}