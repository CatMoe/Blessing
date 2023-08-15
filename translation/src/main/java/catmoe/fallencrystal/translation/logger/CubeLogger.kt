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

package catmoe.fallencrystal.translation.logger

import net.kyori.adventure.text.Component
import java.util.logging.Level

@Suppress("MemberVisibilityCanBePrivate")
object CubeLogger {

    var debug = false
    var logger: ICubeLogger? = null
        get() = field ?: throw NullPointerException("Logger has not been initialized!")

    fun log(level: Level, message: String) { logger!!.log(level, message) }

    fun log(level: Level, component: Component) { logger!!.log(level, component) }

    fun getInstance(): Any? { return logger?.logInstance() }

    fun debug(message: String) { if (debug) logger!!.log(Level.INFO, message) }

}