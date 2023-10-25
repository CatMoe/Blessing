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
import catmoe.fallencrystal.translation.utils.system.impl.CpuUsage

@Suppress("MemberVisibilityCanBePrivate")
class TickingProfile(
    val attackProfile: TickingAttackProfile?,
    val cps: Int,
    val peakCPS: Int,
    val total: Long,
    val totalIPs: Long,
    val blocked: MutableMap<BlockType, Long>,
    val cpu: CpuUsage,
    val incoming: Long,
    val outgoing: Long
)