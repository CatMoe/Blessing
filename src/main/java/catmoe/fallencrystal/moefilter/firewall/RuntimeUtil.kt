package catmoe.fallencrystal.moefilter.firewall

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
