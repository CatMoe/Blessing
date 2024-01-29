/*
 * Copyright (C) 2023-2024. CatMoe / Blessing Contributors
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

package net.miaomoe.blessing.protocol.version

import net.miaomoe.blessing.nbt.dimension.NbtVersion

@Suppress("MemberVisibilityCanBePrivate")
enum class Version(val protocolId: Int, val isSupported: Boolean = true, val registerMap: Boolean = true) {
    UNDEFINED(-1, false, false),
    V1_7_2(4),
    @Deprecated("Use V1_7_2 for this same protocol. (4)", replaceWith = ReplaceWith("V1_7_2"))
    V1_7_3(4, false, false),
    @Deprecated("Use V1_7_2 for this same protocol. (4)", replaceWith = ReplaceWith("V1_7_2"))
    V1_7_4(4, false, false),
    @Deprecated("Use V1_7_2 for this same protocol. (4)", replaceWith = ReplaceWith("V1_7_2"))
    V1_7_5(4, false, false),
    V1_7_6(5, true),
    @Deprecated("Use V1_7_2 for this same protocol. (5)", replaceWith = ReplaceWith("V1_7_6"))
    V1_7_7(5, false, false),
    @Deprecated("Use V1_7_2 for this same protocol. (5)", replaceWith = ReplaceWith("V1_7_6"))
    V1_7_8(5, false, false),
    @Deprecated("Use V1_7_2 for this same protocol. (5)", replaceWith = ReplaceWith("V1_7_6"))
    V1_7_9(5, false, false),
    @Deprecated("Use V1_7_2 for this same protocol. (5)", replaceWith = ReplaceWith("V1_7_6"))
    V1_7_10(5, false, false),
    V1_8(47),
    V1_9(107),
    V1_9_1(108),
    V1_9_2(109),
    @Deprecated("Use V1_9_2 for this same protocol. (109)", replaceWith = ReplaceWith("V1_9_2"))
    V1_9_3(109, registerMap = false),
    V1_9_4(110),
    V1_10(210),
    @Deprecated("Use V1_10 for this same protocol. (210)", replaceWith = ReplaceWith("V1_10"))
    V1_10_1(210, registerMap = false),
    @Deprecated("Use V1_10 for this same protocol. (210)", replaceWith = ReplaceWith("V1_10"))
    V1_10_2(210, registerMap = false),
    V1_11(315),
    V1_11_1(316),
    @Deprecated("Use V1_11_1 for this same protocol. (316)", replaceWith = ReplaceWith("V1_11_1"))
    V1_11_2(316, registerMap = false),
    V1_12(335),
    V1_12_1(338),
    V1_12_2(340),
    V1_13(393),
    V1_13_1(401),
    V1_13_2(404),
    V1_14(477),
    V1_14_1(480),
    V1_14_2(485),
    V1_14_3(490),
    V1_14_4(498),
    V1_15(573),
    V1_15_1(575),
    V1_15_2(578),
    V1_16(735),
    V1_16_1(736),
    V1_16_2(751),
    V1_16_3(753),
    V1_16_4(754),
    @Deprecated("Use V1_16_4 for this same protocol. (754)", replaceWith = ReplaceWith("V1_16_4"))
    V1_16_5(754, registerMap = false),
    V1_17(755),
    V1_17_1(756),
    V1_18(757),
    @Deprecated("Use V1_18 for this same protocol. (757)", replaceWith = ReplaceWith("V1_18"))
    V1_18_1(757, registerMap = false),
    V1_18_2(758),
    V1_19(759),
    V1_19_1(760),
    @Deprecated("Use V1_19_1 for this same protocol. (760)", replaceWith = ReplaceWith("V1_19_1"))
    V1_19_2(760, registerMap = false),
    V1_19_3(761),
    V1_19_4(762),
    V1_20(763),
    @Deprecated("Use V1_20 for this same protocol. (763)", replaceWith = ReplaceWith("V1_20"))
    V1_20_1(763, registerMap = false),
    V1_20_2(764),
    V1_20_3(765),
    @Deprecated("Use V1_20_3 for this same protocol. (765)", replaceWith = ReplaceWith("V1_20_3"))
    V1_20_4(765, registerMap = false);

    fun toNbtVersion() = when {
        this.moreOrEqual(V1_20_2) -> NbtVersion.V1_20_2
        this.moreOrEqual(V1_19_4) -> NbtVersion.V1_19_4
        this.moreOrEqual(V1_19_1) -> NbtVersion.V1_19_1
        this == V1_19 -> NbtVersion.V1_19
        this == V1_18_2 -> NbtVersion.V1_18_2
        this.moreOrEqual(V1_16_2) -> NbtVersion.V1_16_2
        else -> NbtVersion.LEGACY
    }

    fun more(another: Version) = protocolId > another.protocolId
    fun moreOrEqual(another: Version) = protocolId >= another.protocolId
    fun less(another: Version) = protocolId < another.protocolId
    fun lessOrEqual(another: Version) = protocolId <= another.protocolId
    fun fromTo(min: Version, max: Version) = protocolId >= min.protocolId && protocolId <= max.protocolId
    override fun toString() = "Version(name=$name, protocolId=$protocolId, registerMap=$registerMap, isSupported=$isSupported)"
    companion object {
        val maps = entries
            .filter { it.registerMap }
            .associateBy { it.protocolId }
        val min = entries.first { it != UNDEFINED && it.isSupported }
        val max = entries.last { it.registerMap }
        fun of(protocolId: Int) = maps[protocolId] ?: UNDEFINED
    }
}