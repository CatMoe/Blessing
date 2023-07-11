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

package catmoe.fallencrystal.moefilter.common.utils.system

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object RuntimeUtil {
    fun execute(vararg command: String): Process? {
        return try {
            val commands = arrayOf("/bin/bash", "-c") + command
            return ProcessBuilder(*commands).start()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun executeAndGetOutput(command: String): String {
        try {
            val args = arrayOf("/bin/bash", "-c", command)
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
