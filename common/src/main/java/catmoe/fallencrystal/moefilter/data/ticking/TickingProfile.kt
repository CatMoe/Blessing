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