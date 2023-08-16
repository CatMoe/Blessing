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

package catmoe.fallencrystal.moefilter.logger

import catmoe.fallencrystal.moefilter.MoeFilterVelocity
import catmoe.fallencrystal.translation.logger.ICubeLogger
import catmoe.fallencrystal.translation.utils.component.ComponentUtil
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import org.slf4j.Logger
import java.util.logging.Level

class VelocityLogger(val plugin: MoeFilterVelocity, val proxyServer: ProxyServer, val logger: Logger) : ICubeLogger {

    override fun log(level: Level, message: String) {
        when (level) {
            Level.INFO -> logger.info(message)
            Level.WARNING -> logger.warn(message)
            Level.SEVERE -> logger.error(message)
        }
    }

    private val levelMap = mapOf(
        Level.INFO to "<white>[INFO]",
        Level.WARNING to "<yellow>[WARN]",
        Level.SEVERE to "<red>[ERROR]"
    )

    override fun log(level: Level, component: Component) {
        val orig = ComponentUtil.componentToRaw(component)
        val lev = levelMap[level] ?: "<white>[INFO]"
        proxyServer.consoleCommandSource.sendMessage(ComponentUtil.parse("$lev $orig"))
    }

    override fun logInstance(): Any {
        return this
    }
}