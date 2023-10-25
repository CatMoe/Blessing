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

package catmoe.fallencrystal.translation.utils.system

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object CommandUtil {
    fun execute(vararg command: String): Process? {
        return try {
            val commands = arrayOf("/usr/bin/bash", "-c") + command
            return ProcessBuilder(*commands).start()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun executeAndGetOutput(command: String): String {
        try {
            val args = arrayOf("/usr/bin/bash", "-c", command)
            val sb = StringBuilder()
            val proc = ProcessBuilder(*args).start()
            val bfr1 = BufferedReader(InputStreamReader(proc.inputStream))
            val bfr2 = BufferedReader(InputStreamReader(proc.errorStream))
            var str: String?
            while (bfr1.readLine().also { str = it } != null) sb.append(str)
            while (bfr2.readLine().also { str = it } != null) sb.append(str)
            return sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }
}
