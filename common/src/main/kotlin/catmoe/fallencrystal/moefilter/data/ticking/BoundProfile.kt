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

package catmoe.fallencrystal.moefilter.data.ticking

import catmoe.fallencrystal.moefilter.data.BlockType
import catmoe.fallencrystal.translation.utils.config.CreateConfig
import catmoe.fallencrystal.translation.utils.system.impl.CpuUsage
import com.typesafe.config.Config
import java.io.File
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class BoundProfile(val list: MutableMap<Int, TickingProfile>) {

    fun write(profile: TickingProfile) {
        list[list.keys.last()+1]=profile
    }

    companion object {
        fun create(map: MutableMap<Int, TickingProfile>): BoundProfile { return BoundProfile(map) }
        fun create(): BoundProfile { return create(mutableMapOf()) }

        fun writeFile(profile: BoundProfile, folder: File, name: String) {
            val c = CreateConfig(File(folder, "profiles"))
            val list = mutableListOf<String>()
            for (i in profile.list.keys) {
                list.addAll(writeHoconProfile(i, profile.list[i] ?: continue))
            }
            c.setDefaultConfig(list)
            c.setConfigName(name)
            c.onLoad()
        }

        fun writeHoconProfile(id: Int, profile: TickingProfile): List<String> {
            val list = mutableListOf<String>()
            list.add("$id {")
            list.add("  cps=${profile.cps}")
            list.add("  peakCPS=${profile.peakCPS}")
            list.add("  total=${profile.total}")
            list.add("  totalIPs=${profile.totalIPs}")
            list.add("  incoming=${profile.incoming}")
            list.add("  outgoing=${profile.outgoing}")
            list.add("  cpu {")
            val cpu = profile.cpu
            list.add("    system=${cpu.systemCPU}")
            list.add("    process=${cpu.processCPU}")
            list.add("  }")
            val attack = profile.attackProfile
            list.add("  attack-profile {")
            list.add("    available=${attack != null}")
            if (attack != null) {
                list.add("    peakCPS=${attack.sessionPeakCPS}")
                list.add("    total=${attack.sessionTotal}")
                list.add("    totalIPs=${attack.sessionTotalIPs}")
                list.add("    blocked-session {")
                list.addAll(writeBlocked("      ", attack.sessionBlocked))
                list.add("    }")
            }
            list.add("  }")
            list.add("  blocked {")
            list.addAll(writeBlocked("    ", profile.blocked))
            list.add("  }")
            list.add("}")
            return list
        }

        fun readHocon(config: Config) {
            val map: MutableMap<Int, TickingProfile> = HashMap()
            for (i in config.root().keys) {
                val id: Int
                try { id=i.toInt() } catch (_: NumberFormatException) { continue }
                val c = config.getConfig(i)
                var attack: TickingAttackProfile? = null
                if (c.getBoolean("attack-profile.available")) {
                    val c2 = c.getConfig("attack-profile")
                    attack = TickingAttackProfile(c2.getInt("peakCPS"), readBlocked(c2), c2.getLong("total"), c2.getLong("totalIPs"))
                }
                map[id]= TickingProfile(
                    attack,
                    c.getInt("cps"),
                    c.getInt("peakCPS"),
                    c.getLong("total"),
                    c.getLong("totalIPs"),
                    readBlocked(c),
                    CpuUsage(c.getDouble("cpu.process"), c.getDouble("cpu.system")),
                    c.getLong("incoming"),
                    c.getLong("outgoing")
                )
            }
        }

        fun readBlocked(config: Config): MutableMap<BlockType, Long> {
            val blocked: MutableMap<BlockType, Long> = EnumMap(BlockType::class.java)
            for (i in config.getConfigList("blocked")) {
                blocked[BlockType.valueOf(i.getAnyRef("type").toString())]=i.getLong("value")
            }
            return blocked
        }

        fun writeBlocked(prefix: String, blocked: MutableMap<BlockType, Long>): List<String> {
            val list =  mutableListOf<String>()
            for (it in blocked.keys) {
                list.add("$prefix{")
                list.add("$prefix  type=$it")
                list.add("$prefix  value=${blocked[it]}")
                list.add("$prefix}")
            }
            return list
        }
    }
}