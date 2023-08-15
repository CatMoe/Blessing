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

package catmoe.fallencrystal.moefilter.common.firewall.system

import catmoe.fallencrystal.translation.utils.system.CommandUtil
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import net.md_5.bungee.BungeeCord

@Suppress("unused", "MemberVisibilityCanBePrivate")
class FirewallLoader(private val groupName: String, private val timeout: Int) {

    fun initFirewall() {
        if (System.getProperty("os.name").lowercase().contains("win")) { logError("This features is not available on windows.") }
        if (!checkPermission()) { logError("No enough permission to setup firewall. Do you running proxy on non-root user?"); shutdown() }
        if (!checkAvailable()) { logError("Permission denied or iptables & ipset not installed. Check it before setup firewall."); shutdown() }
        start()
    }

    fun checkPermission(): Boolean {
        val result = executeGetOutput("sudo") ?: return false
        return !result.contains("Permission Deined")
    }

    fun checkAvailable(): Boolean {
        val ipset = (executeGetOutput("ipset --version") ?: return false).contains("ipset v")
        val iptables = (executeGetOutput("iptables --version") ?: return false).contains("iptables v")
        return ipset && iptables
    }

    private fun executeGetOutput(command: String): String? { return try { CommandUtil.executeAndGetOutput(command) } catch (_: Exception) { null } }


    fun start() {
        try {
            listOf(
                "sudo ipset create $groupName hash:ip timeout $timeout",
                "sudo iptables -t raw -I PREROUTING -m set --match-set $groupName src -j DROP"
            ).forEach { CommandUtil.execute(it) }
        } catch (ex: Exception) { ex.printStackTrace(); shutdown() }
    }

    fun stop() {
        try {
            listOf(
                "sudo iptables -t raw -D PREROUTING -m set --match-set $groupName src -j DROP",
                "sudo ipset x $groupName"
            ).forEach { CommandUtil.execute(it) }
        } catch (ex: Exception) { ex.printStackTrace(); BungeeCord.getInstance().stop() }
    }

    private fun shutdown() { stop(); shutdown() }

    private fun logError(log: String) { MessageUtil.logError("[MoeFilter] [IPTables] $log") }

    private fun logInfo(log: String) { MessageUtil.logInfo("[MoeFilter] [IPTables] $log") }

}