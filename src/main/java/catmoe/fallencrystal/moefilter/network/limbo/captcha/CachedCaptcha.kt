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

import catmoe.fallencrystal.moefilter.network.limbo.captcha.CaptchaPacketId.*
import catmoe.fallencrystal.moefilter.network.limbo.netty.ByteMessage
import io.netty.buffer.Unpooled
import java.util.*

object CachedCaptcha {

    @JvmField
    var generated: Boolean = false
    private val random = Random()

    val captcha: Queue<CaptchaHolder> = ArrayDeque()

    fun createCaptchaPacket(a: PacketMapData, b: String) {
        val d = CaptchaHolder()
        val c = this.a(a, D)
        d.a=b
        d.b = this.a(a, A)
        d.c = this.a(a, B)
        d.d = this.a(a, C)
        d.e = c
        d.f = this.a(a, E)
        d.g = c
        d.h = this.a(a, G)
        d.i = this.a(a, H)
        d.j = this.a(a, I)
        d.k = this.a(a, J)
        d.l = this.a(a, K)
        d.m = this.a(a, L)
    }

    private fun a(a: PacketMapData, b: CaptchaPacketId): ByteMessage {
        val c = ByteMessage(Unpooled.buffer())
        c.writeVarInt(b.i)
        a.encode(c, b.v)
        c.capacity(c.readableBytes())
        return c
    }

}