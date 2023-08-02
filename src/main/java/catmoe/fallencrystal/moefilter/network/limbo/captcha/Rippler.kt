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

import java.awt.image.BufferedImage
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * A filter to generate ripple (wave) effected images. Uses a transformed sinus
 * wave for this. This class is thread safe.
 *
 * @author akiraly
 */
@Suppress("KDocUnresolvedReference")
open class Rippler
/**
 * Constructor.
 *
 * @param vertical config to calculate waving deltas from x axis (so to
 * modify y values), not null
 * @param horizontal config to calculate waving deltas from y axis (so to
 * modify x values), not null
 */(
    /**
     * @return vertical config, not null
     */
    val vertical: AxisConfig,
    /**
     * @return horizontal config, not null
     */
    val horizontal: AxisConfig
) {
    /**
     * Class to respresent wave tranforming information for an axis.
     */
    open class AxisConfig(start: Double, length: Double, amplitude: Double) {
        /**
         * @return wave part start value
         */
        val start: Double

        /**
         * @return wave part length
         */
        val length: Double

        /**
         * @return amplitude used to transform the wave part
         */
        val amplitude: Double

        /**
         * Constructor.
         *
         * @param start the starting x offset to generate wave values. Should be
         * between 0 and 2 * [Math.PI].
         * @param length the length of x to be used to generate wave values.
         * Should be between 0 and 4 * [Math.PI].
         * @param amplitude the maximum y value, if it is too big, some
         * important parts of the image (like the text) can "wave" out on the
         * top or on the bottom of the image.
         */
        init {
            this.start = normalize(start, 2)
            this.length = normalize(length, 4)
            this.amplitude = amplitude
        }

        /**
         * Normalizes parameter to fall into [0, multi * [Math.PI]].
         *
         * @param a to be normalized
         * @param multi multiplicator used for end value
         * @return normalized value
         */
        private fun normalize(a: Double, multi: Int): Double {
            var a = a
            val piMulti = multi * Math.PI
            a = abs(a)
            val d = floor(a / piMulti)
            return a - d * piMulti
        }
    }

    /**
     * Draws a rippled (waved) variant of source into destination.
     *
     * @param src to be transformed, not null
     * @param dest to hold the result, not null
     * @return dest is returned
     */
    fun filter(src: BufferedImage, dest: BufferedImage): BufferedImage {
        val width = src.width
        val height = src.height
        val verticalDelta = calcDeltaArray(vertical, width)
        val horizontalDelta = calcDeltaArray(horizontal, height)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val ny = (y + verticalDelta[x] + height) % height
                val nx = (x + horizontalDelta[ny] + width) % width
                dest.setRGB(nx, ny, src.getRGB(x, y))
            }
        }
        return dest
    }

    /**
     * Calculates wave delta array.
     *
     * @param axisConfig config object to transform the wave, not null
     * @param num number of points needed, positive
     * @return the calculated num length delta array
     */
    private fun calcDeltaArray(axisConfig: AxisConfig, num: Int): IntArray {
        val delta = IntArray(num)
        val start = axisConfig.start
        val period = axisConfig.length / num
        val amplitude = axisConfig.amplitude
        for (fi in 0 until num) {
            delta[fi] = (amplitude * sin(start + fi * period)).roundToInt()
        }
        return delta
    }
}
