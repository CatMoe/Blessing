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

import catmoe.fallencrystal.moefilter.network.limbo.netty.ByteMessage
import catmoe.fallencrystal.moefilter.network.limbo.util.Version
import catmoe.fallencrystal.moefilter.network.limbo.util.Version.*
import io.netty.channel.Channel

@Suppress("MemberVisibilityCanBePrivate", "unused")
class CaptchaHolder{

    var a: String? = null
    var b: ByteMessage? = null
    var c: ByteMessage? = null
    var d: ByteMessage? = null
    var e: ByteMessage? = null
    var f: ByteMessage? = null
    var g: ByteMessage? = null
    var h: ByteMessage? = null
    var i: ByteMessage? = null
    var j: ByteMessage? = null
    var k: ByteMessage? = null
    var l: ByteMessage? = null
    var m: ByteMessage? = null

    fun n(o: Channel, p: Version, q: Boolean) {
        val r = (if (p == V1_8) b
        else if (p.lessOrEqual(V1_12_2)) c
        else if (p.lessOrEqual(V1_13_2)) d
        else if (p.lessOrEqual(V1_19_4)) e
        else if (p.lessOrEqual(V1_15_2)) f
        else if (p.lessOrEqual(V1_16_1)) g
        else if (p.lessOrEqual(V1_16_4)) h
        else if (p.lessOrEqual(V1_18_2)) i
        else if (p.lessOrEqual(V1_19)) j
        else if (p.lessOrEqual(V1_19_1)) k
        else if (p.lessOrEqual(V1_19_3)) l
        else if (p.lessOrEqual(V1_20)) m
        else null) ?: return
        o.write(r, o.voidPromise())
        if (q) { o.flush() }
    }
}
