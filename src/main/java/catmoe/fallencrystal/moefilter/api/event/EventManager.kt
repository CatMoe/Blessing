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

package catmoe.fallencrystal.moefilter.api.event

import catmoe.fallencrystal.moefilter.MoeFilter
import com.github.benmanes.caffeine.cache.Caffeine
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import java.lang.reflect.Method
import java.util.concurrent.CopyOnWriteArrayList

@Suppress("unused")
object EventManager {

    private val listeners: MutableList<EventListener> = CopyOnWriteArrayList()
    private val plugin = MoeFilter.instance as Plugin
    private val scheduler = ProxyServer.getInstance().scheduler
    private val listenerPlugin = Caffeine.newBuilder().build<EventListener, Plugin>()

    fun triggerEvent(event: MoeAsyncEvent) { scheduler.runAsync(plugin) { listeners.forEach { methodTrigger(it, event) } } }

    fun triggerEvent(event: MoeEvent) { listeners.forEach { methodTrigger(it, event) } }

    private fun methodTrigger(listener: EventListener, event: Any) {
        val methods = listener.javaClass.declaredMethods
        val lPlugin = listenerPlugin.getIfPresent(listener) ?: plugin
        if (event is MoeAsyncEvent) { methods.forEach { if (needSend(event, it)) { scheduler.runAsync(lPlugin) {  try { it.invoke(listener, event) } catch (e: Exception) { e.printStackTrace() } } } } }
        if (event is MoeEvent) { methods.forEach { try { it.invoke(listener, event) } catch (e: Exception) { e.printStackTrace() } } }
    }

    private fun needSend(event: Any, method: Method): Boolean { return method.isAnnotationPresent(FilterEvent::class.java) && method.parameterCount == 1 && event::class.java.isAssignableFrom(method.parameterTypes[0]) }

    @Deprecated("Use method registerListener(Plugin, EventListener)")
    fun registerListener(c: EventListener) { listeners.add(c) }

    fun registerListener(p: Plugin, c: EventListener) { listenerPlugin.put(c, p); listeners.add(c) }

    fun unregisterListener(c: EventListener) {
        scheduler.runAsync(plugin) {
            val listenerToRemove = mutableListOf<EventListener>()
            listeners.forEach { if (it::class.java == c::class.java) { listenerToRemove.add(it); listenerPlugin.invalidate(it) } }
            listeners.removeAll(listenerToRemove)
        }
    }

    fun listenerList(): MutableList<EventListener> { return listeners }
}
