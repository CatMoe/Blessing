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
package catmoe.fallencrystal.translation.utils.version

@Suppress("unused")
enum class Version(val number: Int) {
    UNDEFINED(-1),
    V1_7_6(5),
    V1_8(47),
    V1_9(107),
    V1_9_1(108),
    V1_9_2(109),
    V1_9_4(110),
    V1_10(210),
    V1_11(315),
    V1_11_1(316),
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
    V1_17(755),
    V1_17_1(756),
    V1_18(757),
    V1_18_2(758),
    V1_19(759),
    V1_19_1(760),
    V1_19_3(761),
    V1_19_4(762),
    V1_20(763),
    V1_20_2(764);

    var prev: Version? = null
        private set

    fun more(another: Version): Boolean { return number > another.number }

    fun moreOrEqual(another: Version): Boolean { return number >= another.number }

    fun less(another: Version): Boolean { return number < another.number }

    fun lessOrEqual(another: Version): Boolean { return number <= another.number }

    fun fromTo(min: Version, max: Version): Boolean { return number >= min.number && number <= max.number }

    val isSupported: Boolean
        get() = (this != UNDEFINED) // && (this != V1_20_2)

    override fun toString(): String {
        return "enum=$name, protocol=$number"
    }

    companion object {
        private var VERSION_MAP: MutableMap<Int, Version>? = null
        var max = V1_20_2

        init {
            val values = entries.toTypedArray()
            VERSION_MAP = HashMap()
            max = values[values.size - 1]
            var last: Version? = null
            for (version in values) {
                version.prev = last
                last = version
                (VERSION_MAP as HashMap<Int, Version>)[version.number] = version
            }
        }

        val min = V1_7_6

        fun of(protocolNumber: Int): Version {
            return VERSION_MAP!!.getOrDefault(protocolNumber, UNDEFINED)
        }
    }
}
