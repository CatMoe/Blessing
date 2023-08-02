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

import catmoe.fallencrystal.moefilter.network.limbo.captcha.CachedCaptcha.createCaptchaPacket
import catmoe.fallencrystal.moefilter.network.limbo.captcha.MapPalette.colors
import catmoe.fallencrystal.moefilter.network.limbo.captcha.MapPalette.prepareColors
import java.awt.Color
import java.awt.Font
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

@Suppress("unused")
class CaptchaGeneration {
    private var rnd = Random()
    fun generateImages() {
        val fonts = arrayOf(
            Font(Font.SANS_SERIF, Font.PLAIN, 50),
            Font(Font.SERIF, Font.PLAIN, 50),
            Font(Font.MONOSPACED, Font.BOLD, 50)
        )
        val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        val painter = CaptchaPainter()
        prepareColors()
        for (i in 0..10) {
            executor.execute {
                if (CachedCaptcha.captcha.size > 10) return@execute
                val answer = randomAnswer()
                val image = painter.draw(fonts[rnd.nextInt(fonts.size)], randomNotWhiteColor(), answer)
                val map = CraftMapCanvas()
                map.drawImage(0, 0, image)
                createCaptchaPacket(CraftMapCanvas().mapData, answer)
            }
        }
        executor as ThreadPoolExecutor
        CachedCaptcha.generated = true
        executor.shutdownNow()
        System.gc()
    }

    private fun randomNotWhiteColor(): Color {
        val color = colors[rnd.nextInt(colors.size)]
        val r = color.red
        val g = color.green
        val b = color.blue
        if (r == 255 && g == 255 && b == 255) {
            return randomNotWhiteColor()
        }
        if (r == 220 && g == 220 && b == 220) {
            return randomNotWhiteColor()
        }
        if (r == 199 && g == 199 && b == 199) {
            return randomNotWhiteColor()
        }
        if (r == 255 && g == 252 && b == 245) {
            return randomNotWhiteColor()
        }
        if (r == 220 && g == 217 && b == 211) {
            return randomNotWhiteColor()
        }
        return if (r == 247 && g == 233 && b == 163) {
            randomNotWhiteColor()
        } else color
    }

    private fun randomAnswer(): String {
        return if (rnd.nextBoolean()) {
            (rnd.nextInt(99999 - 10000 + 1) + 10000).toString()
        } else {
            (rnd.nextInt(9999 - 1000 + 1) + 1000).toString()
        }
    }
}
