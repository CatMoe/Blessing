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

package catmoe.fallencrystal.moefilter.network.limbo.captcha

import catmoe.fallencrystal.moefilter.network.limbo.util.Version
import catmoe.fallencrystal.moefilter.network.limbo.util.Version.*

@Suppress("unused")
enum class CaptchaPacketId(@JvmField val i: Int, @JvmField val v: Version) {
    A(0x34, V1_8),
    B(0x24, V1_9),
    C(0x26, V1_13),
    D(0x26, V1_14),
    E(0x27, V1_15),
    F(0x26, V1_16),
    G(0x25, V1_16_2),
    H(0x27, V1_17),
    I(0x24, V1_19),
    J(0x26, V1_19_1),
    K(0x25, V1_19_3),
    L(0x29, V1_19_4)
}