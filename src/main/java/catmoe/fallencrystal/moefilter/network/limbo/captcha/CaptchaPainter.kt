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

import catmoe.fallencrystal.moefilter.network.limbo.captcha.Rippler.AxisConfig
import java.awt.*
import java.awt.font.GlyphVector
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.awt.image.ConvolveOp
import java.awt.image.Kernel
import java.util.*
import kotlin.math.min
import kotlin.math.sign

@Suppress("UNUSED_VARIABLE", "UNUSED_PARAMETER")
class CaptchaPainter {
    private val width = 128
    private val height = 128
    private val background = Color.WHITE
    private val rnd = Random()
    fun draw(font: Font?, fGround: Color?, text: String?): BufferedImage {
        requireNotNull(font) { "Font can not be null." }
        requireNotNull(fGround) { "Foreground color can not be null." }
        require(!text.isNullOrEmpty()) { "No text given." }
        var img = createImage()
        val g = img.graphics
        try {
            val g2 = configureGraphics(g, font, fGround)
            draw(g2, text)
        } finally {
            g.dispose()
        }
        img = postProcess(img)
        return img
    }

    private fun createImage(): BufferedImage {
        return BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
    }

    private fun configureGraphics(g: Graphics, font: Font?, fGround: Color?): Graphics2D {
        check(g is Graphics2D) {
            ("Graphics (" + g
                    + ") that is not an instance of Graphics2D.")
        }
        configureGraphicsQuality(g)
        g.color = fGround
        g.background = background
        g.font = font
        g.clearRect(0, 0, width, height)
        return g
    }

    private fun configureGraphicsQuality(g2: Graphics2D) {
        g2.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        )
        g2.setRenderingHint(
            RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON
        )
        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )
        g2.setRenderingHint(
            RenderingHints.KEY_COLOR_RENDERING,
            RenderingHints.VALUE_COLOR_RENDER_QUALITY
        )
        g2.setRenderingHint(
            RenderingHints.KEY_DITHERING,
            RenderingHints.VALUE_DITHER_ENABLE
        )
        g2.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC
        )
        g2.setRenderingHint(
            RenderingHints.KEY_ALPHA_INTERPOLATION,
            RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY
        )
        g2.setRenderingHint(
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY
        )
    }

    private fun draw(g: Graphics2D, text: String?) {
        val vector = g.font.createGlyphVector(
            g.fontRenderContext, text
        )
        transform(g, text, vector)
        val bounds = vector.getPixelBounds(null, 0f, height.toFloat())
        val bw = bounds.getWidth().toFloat()
        val bh = bounds.getHeight().toFloat()
        val outlineEnabled = true
        val wr = (width / bw * (rnd.nextFloat() / 20 + 0.89f)
                * 1)
        val hr = (height / bh * (rnd.nextFloat() / 20 + 0.68f)
                * 1)
        g.translate(((width - bw * wr) / 2).toDouble(), ((height - bh * hr) / 2).toDouble())
        g.scale(wr.toDouble(), hr.toDouble())
        val bx = bounds.getX().toFloat()
        val by = bounds.getY().toFloat()
        g.draw(
            vector.getOutline(
                (sign(rnd.nextFloat() - 0.5f) * 1
                        * width) / 200 - bx, (sign(rnd.nextFloat() - 0.5f) * 1
                        * height) / 70 + height - by
            )
        )
        g.drawGlyphVector(vector, -bx, height - by)
    }

    private fun transform(g: Graphics2D?, text: String?, v: GlyphVector) {
        val glyphNum = v.numGlyphs
        var prePos: Point2D? = null
        var preBounds: Rectangle2D? = null
        var rotateCur = (rnd.nextDouble() - 0.5) * Math.PI / 8
        var rotateStep = (sign(rotateCur)
                * (rnd.nextDouble() * 3 * Math.PI / 8 / glyphNum))
        for (fi in 0 until glyphNum) {
            val tr = AffineTransform
                .getRotateInstance(rotateCur)
            if (rnd.nextDouble() < 0.25) {
                rotateStep *= -1.0
            }
            rotateCur += rotateStep
            v.setGlyphTransform(fi, tr)
            val pos = v.getGlyphPosition(fi)
            val bounds = v.getGlyphVisualBounds(fi).bounds2D
            val newPos: Point2D = if (prePos == null) {
                Point2D.Double(
                    pos.x - bounds.x,
                    pos.y
                )
            } else {
                Point2D.Double(
                    (preBounds!!.maxX
                            + pos.x) - bounds.x
                            - (min(
                        preBounds.width,
                        bounds.width
                    )
                            * (rnd.nextDouble() / 20 + 0.27)), pos.y
                )
            }
            v.setGlyphPosition(fi, newPos)
            prePos = newPos
            preBounds = v.getGlyphVisualBounds(fi).bounds2D
        }
    }

    private fun postProcess(img: BufferedImage): BufferedImage {
        var image = img
        run {
            val vertical = AxisConfig(
                rnd.nextDouble() * 2 * Math.PI, (1 + rnd.nextDouble() * 2) * Math.PI, image.height / 10.0
            )
            val horizontal = AxisConfig(
                rnd.nextDouble() * 2 * Math.PI, (2 + rnd.nextDouble() * 2) * Math.PI, image.width / 100.0
            )
            val op = Rippler(vertical, horizontal)

            // img = op.filter( img, createImage() );
            image = op.filter(image, createImage())
        }
        val blurArray = FloatArray(9)
        fillBlurArray(blurArray)
        val op = ConvolveOp(
            Kernel(3, 3, blurArray),
            ConvolveOp.EDGE_NO_OP, null
        )
        image = op.filter(image, createImage())
        return image
    }

    private fun fillBlurArray(array: FloatArray) {
        var sum = 0f
        for (fi in array.indices) {
            array[fi] = rnd.nextFloat()
            sum += array[fi]
        }
        for (fi in array.indices) { array[fi] /= sum }
    }
}
