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

package net.miaomoe.blessing.event.adapter

import net.miaomoe.blessing.event.event.BlessingEvent
import java.lang.reflect.Method
import kotlin.reflect.KClass

class ReflectionListenerAdapter(
    private val owner: Any,
    private val method: Method
) : ListenerAdapter {

    init {
        method.isAccessible=true
        require(method.parameterCount == 1) { "ParameterCount must be 1!" }
        require(BlessingEvent::class.java.isAssignableFrom(method.parameterTypes[0])) { "The Parameter must be BlessingEvent!" }
    }

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION)
    annotation class EventHandler(val event: KClass<out BlessingEvent>, val autoRegister: Boolean = true)

    override fun invoke(event: BlessingEvent) {
        method.invoke(owner, event)
    }

}