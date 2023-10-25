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

        val platformAnnotation = Platform::class.java

        fun canAccess(obj: Any?): Boolean {
            if (obj == null || obj is Field) return true
            val platformAnnotation = when (obj) {
                is Method -> obj.getAnnotation(platformAnnotation)
                is Class<*> -> obj.getAnnotation(platformAnnotation)
                is KClass<*> -> obj.java.getAnnotation(platformAnnotation)
                else -> obj::class.java.getAnnotation(platformAnnotation)
            }
            return (platformAnnotation ?: return true).platform == instance.loader.platform
        }


        inline fun <reified T> secureAccess(obj: T?): T? {
            return if (obj == null || !T::class.java.isAnnotationPresent(platformAnnotation) || obj is Method) obj
            else if (T::class.java.getAnnotation(platformAnnotation).platform != instance.loader.platform) null else obj
        }
    }


}