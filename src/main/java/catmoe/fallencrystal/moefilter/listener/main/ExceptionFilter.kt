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

package catmoe.fallencrystal.moefilter.listener.main

import catmoe.fallencrystal.moefilter.api.logger.ILogger
import java.util.logging.LogRecord

class ExceptionFilter : ILogger {

    private val filterException = listOf(
        "QuietException",
        "FastException",
        "BadPacketException",
        "FastOverflowPacketException",
    )

    override fun isLoggable(record: LogRecord?, isCancelled: Boolean): Boolean {
        if (isCancelled || record == null) return false
        return if (record.thrown?.cause != null) { !filterException.contains(record.thrown.cause!!::class.java.name) } else record.message?.contains("InitialHandler - encountered exception") != true
    }
}