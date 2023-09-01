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

package catmoe.fallencrystal.translation.event

import catmoe.fallencrystal.translation.TranslationLoader
import catmoe.fallencrystal.translation.event.annotations.*
import catmoe.fallencrystal.translation.logger.CubeLogger
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
        val a = this.method.getIfPresent(event::class) ?: return
        for (it in HandlerPriority.values()) { method.addAll((a[it] ?: continue)) }
        if (method.isEmpty()) return
        val cancelRead = AtomicBoolean(false)
        for (it in method) {
            if (!TranslationLoader.canAccess(it)) continue
            val m = listener.getIfPresent(it) ?: continue
            if (cancelRead.get() && !it.isAnnotationPresent(IgnoreCancelled::class.java)) continue
            if (it.isAnnotationPresent(AsynchronousHandler::class.java)) {
                if (it.isAnnotationPresent(EndCallWhenCancelled::class.java))
                    // I swear i will create a better logger xd.
                    CubeLogger.log(Level.WARNING, "Cannot apply annotation \"EndCallWhenCancelled\" for Async event handler (${m.javaClass.name})")
                CompletableFuture.runAsync { try { it.invoke(m, event) } catch (e: Exception) {
                    /* IDK why i will create this.. */
                    if (it.isAnnotationPresent(SilentException::class.java)) {
                        val ignore = it.getAnnotation(SilentException::class.java).exception
                        if (!ignore.contains(e::class)) e.printStackTrace()
                    } else e.printStackTrace()
                } }; continue
            }
            try { it.invoke(m, event) } catch (e: Exception) {
                if (it.isAnnotationPresent(SilentException::class.java)) {
                    val ignore = it.getAnnotation(SilentException::class.java).exception
                    if (!ignore.contains(e::class)) e.printStackTrace() // If they have Exception::class, They will ignore all exception(s)?
                } else e.printStackTrace()
            }
            if (event.isCancelled() == true) cancelRead.set(true)
        }
        if (cancelRead.get()) event.ifCancelled()
    }

    fun register(listener: EventListener) {
        val a: MutableCollection<Method> = CopyOnWriteArrayList()
        val method = listener.javaClass.declaredMethods
        for (it in method) {
            /* it.parameterTypes[0]::class.java.isAssignableFrom(TranslationEvent::class.java) */
            if (it.isAnnotationPresent(EventHandler::class.java) && it.parameterCount == 1) a.add(it)
        }
        for (it in a) {
            val annotation = it.getAnnotation(EventHandler::class.java)
            val h = annotation.priority
            val c: KClass<out TranslationEvent> = annotation.event
            val o = (this.method.getIfPresent(c) ?: mutableMapOf()).toMutableMap()
            val o2 = if (o[h].isNullOrEmpty()) mutableListOf() else o[h]!!
            o2.add(it); this.listener.put(it, listener)
            o[h]=o2
            this.method.put(c, o)
            val k1 = this.kcl.getIfPresent(listener::class) ?: mutableListOf()
            k1.add(it)
            this.kcl.put(listener::class, k1)
        }
    }


    fun unregister(listener: EventListener) {
        val a = this.kcl.getIfPresent(listener::class) ?: return
        val z: MutableCollection<Method> = ArrayList()
        for (it in a) {
            val annotation = it.getAnnotation(EventHandler::class.java)
            val e = annotation.event
            val b = this.method.getIfPresent(e) ?: continue
            val h = annotation.priority
            if (it.parameterCount != 1) continue
            if (!it.parameterTypes[0].isAssignableFrom(e.java)) continue
            val v = (b[h] ?: continue)
            v.remove(it)
            b[h]=v
            this.method.put(e, b)
            z.add(it)
        }
        a.removeAll(z)
        this.kcl.put(listener::class, a)
        if (a.isEmpty()) this.kcl.invalidate(listener::class)
    }

}