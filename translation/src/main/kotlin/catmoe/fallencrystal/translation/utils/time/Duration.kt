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