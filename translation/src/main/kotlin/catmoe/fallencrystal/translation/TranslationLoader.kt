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

package catmoe.fallencrystal.translation

import catmoe.fallencrystal.translation.event.EventManager
import catmoe.fallencrystal.translation.event.events.translation.TranslationLoadEvent
import catmoe.fallencrystal.translation.event.events.translation.TranslationShutdownEvent
import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.server.ServerInstance
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KClass

class TranslationLoader(val loader: CPlatform) {

    init { instance=this }

    @Suppress("DEPRECATION")
    fun load() {
        ServerInstance.init()
        EventManager.callEvent(TranslationLoadEvent())
    }

    fun unload() {
        EventManager.callEvent(TranslationShutdownEvent())
    }

    companion object {
        lateinit var instance: TranslationLoader
            private set

        fun canAccess(obj: Any?): Boolean {
            if (obj == null || obj is Field) return true
            if (obj is Method && obj.isAnnotationPresent(Platform::class.java)) { return obj.getAnnotation(Platform::class.java).platform == instance.loader.platform }
            if (obj is Class<out Any> && obj.isAnnotationPresent(Platform::class.java)) { return obj.getAnnotation(Platform::class.java).platform == instance.loader.platform }
            if (obj is KClass<out Any> && obj.java.isAnnotationPresent(Platform::class.java)) { return obj.java.getAnnotation(Platform::class.java).platform == instance.loader.platform }
            return if (obj::class.java.isAnnotationPresent(Platform::class.java)) obj::class.java.getAnnotation(Platform::class.java).platform == instance.loader.platform else true
        }

        fun secureAccess(obj: Any?): Any? {
            return if (obj == null || !obj::class.java.isAnnotationPresent(Platform::class.java) || obj is Method) obj
            else if (obj::class.java.getAnnotation(Platform::class.java).platform != instance.loader.platform) null else obj
        }
    }


}