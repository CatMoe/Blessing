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

package catmoe.fallencrystal.translation.event

import catmoe.fallencrystal.translation.TranslationLoader
import catmoe.fallencrystal.translation.event.annotations.*
import catmoe.fallencrystal.translation.logger.CubeLogger
import catmoe.fallencrystal.translation.utils.config.LocalConfig
import com.github.benmanes.caffeine.cache.Caffeine
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level
import kotlin.reflect.KClass

object EventManager {

    private val method = Caffeine.newBuilder().build<KClass<out TranslationEvent>, MutableMap<HandlerPriority, MutableCollection<Method>>>()
    private val listener = Caffeine.newBuilder().build<Method, EventListener>()
    private val kcl = Caffeine.newBuilder().build<KClass<out EventListener>, MutableCollection<Method>>()

    @Suppress("DEPRECATION")
    fun callEvent(event: TranslationEvent) {
        val method: MutableCollection<Method> = CopyOnWriteArrayList()
        debug("Trying call handler for event $event (${event.javaClass.name})")
        val a = this.method.getIfPresent(event::class)
        if (a == null) {
            debug("No any handler found.")
            return
        }
        for (it in HandlerPriority.entries) { method.addAll((a[it] ?: continue)) }
        if (method.isEmpty()) {
            debug("No any handler found.")
            return
        }
        val cancelRead = AtomicBoolean(false)
        for (it in method) {
            debug("Trying call method ${it.name} for event ${event.javaClass.name}")
            if (!TranslationLoader.canAccess(it)) {
                debug("Cannot access target. May because target's loader platform is not same to TranslateLoader.")
                continue
            }
            val m = listener.getIfPresent(it)
            if (m == null) {
                debug("No listener class found for method ${it.name}. Skipping")
                continue
            } else {
                debug("Listener ${m.javaClass.name} for method ${it.name} was founded.")
            }
            if (cancelRead.get() && !it.isAnnotationPresent(IgnoreCancelled::class.java)) {
                debug("Cancelling call event for ${it.name} because this event is set cancelled.")
                continue
            }
            if (it.isAnnotationPresent(AsynchronousHandler::class.java)) {
                if (it.isAnnotationPresent(EndCallWhenCancelled::class.java))
                    // I swear i will create a better logger xd.
                    CubeLogger.log(Level.WARNING, "Cannot apply annotation \"EndCallWhenCancelled\" for Async event handler (${m.javaClass.name})")
                debug("Target has async call annotation.")
                debug("Try calling..")
                CompletableFuture.runAsync { try {
                    it.isAccessible=true
                    it.invoke(m, event)
                } catch (e: Exception) {
                    /* IDK why I will create this... */
                    if (it.isAnnotationPresent(SilentException::class.java)) {
                        val ignore = it.getAnnotation(SilentException::class.java).exception
                        if (!ignore.contains(e::class)) e.printStackTrace()
                    } else e.printStackTrace()
                } }; continue
            }
            debug("Try calling..")
            try {
                it.isAccessible=true
                it.invoke(m, event)
            } catch (e: Exception) {
                if (it.isAnnotationPresent(SilentException::class.java)) {
                    val ignore = it.getAnnotation(SilentException::class.java).exception
                    if (!ignore.contains(e::class)) e.printStackTrace() // If they have Exception::class, They will ignore all exception(s)?
                } else e.printStackTrace()
            }
            if (event.isCancelled() == true) { cancelRead.set(true); debug("Event handler ${it.name} set event cancelled.") }
        }
        if (cancelRead.get()) event.ifCancelled()
    }

    fun register(listener: EventListener) {
        if (!TranslationLoader.canAccess(listener)) return
        debug("Listener ${listener.javaClass.name} is trying register.")
        val a: MutableCollection<Method> = CopyOnWriteArrayList()
        val method = listener.javaClass.declaredMethods
        for (it in method) {
            /* it.parameterTypes[0]::class.java.isAssignableFrom(TranslationEvent::class.java) */
            it.isAccessible=true
            if (it.isAnnotationPresent(EventHandler::class.java) && it.parameterCount == 1 && TranslationLoader.canAccess(it)) {
                debug("Founded handler method: ${it.name}")
                a.add(it)
            }
        }
        for (it in a) {
            it.isAccessible=true
            val annotation = it.getAnnotation(EventHandler::class.java)
            val h = annotation.priority
            val c: KClass<out TranslationEvent> = annotation.event
            debug("Trying register event ${c.qualifiedName} (${h.name}) for handler ${it.name}")
            val o = (this.method.getIfPresent(c) ?: mutableMapOf()).toMutableMap()
            val o2 = if (o[h].isNullOrEmpty()) mutableListOf() else o[h]!!
            o2.add(it); this.listener.put(it, listener)
            debug("Registered event ${it.name} for listener ${listener.javaClass.name}")
            o[h]=o2
            this.method.put(c, o)
            val k1 = this.kcl.getIfPresent(listener::class) ?: mutableListOf()
            k1.add(it)
            this.kcl.put(listener::class, k1)
            debug("Created listener index.")
        }
    }


    fun unregister(listener: EventListener) {
        debug("Listener ${listener.javaClass.name} try to unregister.")
        val a = this.kcl.getIfPresent(listener::class)
        if (a == null) { debug("No found any listener method on this listener."); return }
        val z: MutableCollection<Method> = ArrayList()
        debug("Ready to collect need unregister methods")
        for (it in a) {
            val annotation = it.getAnnotation(EventHandler::class.java)
            val e = annotation.event
            debug("Unregistering method ${it.name}, event: ${e.qualifiedName}")
            val b = this.method.getIfPresent(e)
            if (b == null) { debug("This event not registered any method."); continue }
            val h = annotation.priority
            if (it.parameterCount != 1) continue
            if (!it.parameterTypes[0].isAssignableFrom(e.java)) continue
            debug("Try to remove method..")
            val v = (b[h] ?: continue)
            v.remove(it)
            b[h]=v
            this.method.put(e, b)
            z.add(it)
        }
        debug("Removed ${z.size} listener method(s) for listener.")
        a.removeAll(z.toSet())
        this.kcl.put(listener::class, a)
        if (a.isEmpty()) this.kcl.invalidate(listener::class)
    }

    fun debug(message: String) { if (LocalConfig.getConfig().getBoolean("debug")) CubeLogger.log(Level.WARNING, "[Event] $message") }

}