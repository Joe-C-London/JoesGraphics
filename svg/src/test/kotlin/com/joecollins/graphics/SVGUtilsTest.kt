package com.joecollins.graphics

import com.joecollins.graphics.utils.RenderTestUtils
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import javax.swing.JPanel

class SVGUtilsTest {

    @Test
    fun testCreateShape() {
        val shape = SVGUtils.createShape("m-90 2030 45-863a95 95 0 0 0-111-98l-859 151 116-320a65 65 0 0 0-20-73l-941-762 212-99a65 65 0 0 0 34-79l-186-572 542 115a65 65 0 0 0 73-38l105-247 423 454a65 65 0 0 0 111-57l-204-1052 327 189a65 65 0 0 0 91-27l332-652 332 652a65 65 0 0 0 91 27l327-189-204 1052a65 65 0 0 0 111 57l423-454 105 247a65 65 0 0 0 73 38l542-115-186 572a65 65 0 0 0 34 79l212 99-941 762a65 65 0 0 0-20 73l116 320-859-151a95 95 0 0 0-111 98l45 863z")
        val panel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                (g as Graphics2D).apply {
                    setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON,
                    )
                    setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
                    )
                    setRenderingHint(
                        RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY,
                    )
                }
                g.color = Color.WHITE
                g.fillRect(0, 0, width, height)
                g.color = Color.RED
                g.fill(
                    shape.let {
                        val scale = height / it.bounds2D.height
                        AffineTransform.getScaleInstance(scale, scale).createTransformedShape(it)
                    }.let {
                        AffineTransform.getTranslateInstance(-it.bounds2D.x, -it.bounds2D.y).createTransformedShape(it)
                    },
                )
            }
        }
        panel.size = Dimension(200, 100)
        RenderTestUtils.compareRendering("SVGUtils", "createShape", panel)
    }

    @Test
    fun testParseSvg() {
        val svg = SVGUtils.parseSvg(SVGUtilsTest::class.java.classLoader.getResource("com/joecollins/graphics/SVGUtils/canada-flag.svg").toURI())
        val panel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                (g as Graphics2D).apply {
                    setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON,
                    )
                    setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
                    )
                    setRenderingHint(
                        RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY,
                    )
                }
                g.color = Color.WHITE
                g.fillRect(0, 0, width, height)
                g.color = Color.RED
                val originalTransform = g.transform
                g.transform = (height / svg.bounds.height).let { AffineTransform.getScaleInstance(it, it) }
                    .apply { preConcatenate(originalTransform) }
                svg.paint(g)
            }
        }
        panel.size = Dimension(200, 100)
        RenderTestUtils.compareRendering("SVGUtils", "parseSvg", panel)
    }
}
