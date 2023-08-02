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

import catmoe.fallencrystal.moefilter.network.limbo.captcha.MapPalette.imageToBytes
import java.awt.image.BufferedImage
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class CraftMapCanvas {
    private val buffer: ByteArray = mcPixelsBuffer.get()

    init {
        // Arrays.fill(buffer, -1.toByte())
        Arrays.fill(buffer, (-1).toByte())
    }

    fun setPixel(x: Int, y: Int, color: Byte) {
        if (x >= 0 && y >= 0 && x < 128 && y < 128) {
            if (buffer[y * 128 + x] != color) { buffer[y * 128 + x] = color }
        }
    }

    @Suppress("DEPRECATION")
    fun drawImage(x: Int, y: Int, image: BufferedImage) {
        val bytes = imageToBytes(image)
        val width = image.getWidth(null)
        val height = image.getHeight(null)
        for (x2 in 0 until width) {
            for (y2 in 0 until height) { setPixel(x + x2, y + y2, bytes[y2 * width + x2].toByte()) }
        }
    }

    val mapData: PacketMapData
        get() {
            val p = PacketMapData()
            p.columns = 128
            p.rows = 128
            p.x = 0
            p.y = 0
            p.data = buffer
            return p
        }

    companion object {
        private val mcPixelsBuffer = ThreadLocal.withInitial { ByteArray(128 * 128) }
    }
}
