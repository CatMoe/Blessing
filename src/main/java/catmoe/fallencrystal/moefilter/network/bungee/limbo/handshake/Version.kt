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
package catmoe.fallencrystal.moefilter.network.bungee.limbo.handshake

enum class Version(val protocolNumber: Int) {
    UNDEFINED(-1),
    V1_7_2(4),
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
    V1_20(763);

    var prev: Version? = null
        private set

    fun more(another: Version): Boolean { return protocolNumber > another.protocolNumber }

    fun moreOrEqual(another: Version): Boolean { return protocolNumber >= another.protocolNumber }

    fun less(another: Version): Boolean { return protocolNumber < another.protocolNumber }

    fun lessOrEqual(another: Version): Boolean { return protocolNumber <= another.protocolNumber }

    fun fromTo(min: Version, max: Version): Boolean { return protocolNumber >= min.protocolNumber && protocolNumber <= max.protocolNumber }

    val isSupported: Boolean
        get() = this != UNDEFINED

    companion object {
        private var VERSION_MAP: MutableMap<Int, Version>? = null
        var max: Version? = null

        init {
            val values = values()
            VERSION_MAP = HashMap()
            max = values[values.size - 1]
            var last: Version? = null
            for (version in values) {
                version.prev = last
                last = version
                (VERSION_MAP as HashMap<Int, Version>)[version.protocolNumber] = version
            }
        }

        val min: Version
            get() = V1_7_2

        fun of(protocolNumber: Int): Version {
            return VERSION_MAP!!.getOrDefault(protocolNumber, UNDEFINED)
        }
    }
}
