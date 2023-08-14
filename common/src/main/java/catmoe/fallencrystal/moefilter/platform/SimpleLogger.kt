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

package catmoe.fallencrystal.moefilter.platform

import java.util.logging.Level

class SimpleLogger(val logger: Any) {

    fun log(level: Level, message: String) {
        try {
            if (logger is java.util.logging.Logger) { logger.log(level, message); return }
            else if (logger is org.slf4j.Logger) {
                when (level) {
                    Level.INFO -> logger.info(message)
                    Level.WARNING -> logger.info(message)
                    Level.SEVERE -> logger.error(message)
                    else -> logger.info(message)
                }
            }
        } catch (_: NoClassDefFoundError) { }
    }

}