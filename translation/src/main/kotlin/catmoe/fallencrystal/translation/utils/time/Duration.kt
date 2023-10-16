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

package catmoe.fallencrystal.translation.utils.time

import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate")
class Duration {

    private var startTime: Long = 0

    fun start() {
        startTime = System.currentTimeMillis()
    }

    fun getDuration(): Long {
        if (startTime == 0.toLong()) return 0
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)
    }

    fun getFormat(): String { return getFormat(getDuration()) }

    fun stop() { startTime = 0 }

    companion object {
        fun getFormat(sec: Long): String {
            return if (sec >= 3600) String.format("%02d:%02d:%02d", sec / 3600, sec % 3600 / 60, sec % 60)
            else String.format("%02d:%02d", sec % 3600 / 60, sec % 60)
        }
    }
}