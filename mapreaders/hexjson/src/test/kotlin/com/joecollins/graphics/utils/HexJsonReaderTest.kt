package com.joecollins.graphics.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.AffineTransform
import javax.swing.JPanel
import kotlin.math.roundToInt

class HexJsonReaderTest {

    @Test
    fun testOddR() {
        val shapes = HexJsonReader.readHex(HexJsonReaderTest::class.java.classLoader.getResource("com/joecollins/graphics/hexjsons/odd-r.json"))
        assertEquals(('A'..'J').map { it.toString() }.toSet(), shapes.keys)
        assertShapes(shapes, "OddR")
    }

    @Test
    fun testEvenR() {
        val shapes = HexJsonReader.readHex(HexJsonReaderTest::class.java.classLoader.getResource("com/joecollins/graphics/hexjsons/even-r.json"))
        assertEquals(('A'..'J').map { it.toString() }.toSet(), shapes.keys)
        assertShapes(shapes, "EvenR")
    }

    @Test
    fun testOddQ() {
        val shapes = HexJsonReader.readHex(HexJsonReaderTest::class.java.classLoader.getResource("com/joecollins/graphics/hexjsons/odd-q.json"))
        assertEquals(('A'..'J').map { it.toString() }.toSet(), shapes.keys)
        assertShapes(shapes, "OddQ")
    }

    @Test
    fun testEvenQ() {
        val shapes = HexJsonReader.readHex(HexJsonReaderTest::class.java.classLoader.getResource("com/joecollins/graphics/hexjsons/even-q.json"))
        assertEquals(('A'..'J').map { it.toString() }.toSet(), shapes.keys)
        assertShapes(shapes, "EvenQ")
    }

    private fun assertShapes(shapes: Map<String, Shape>, testMethod: String) {
        val scaleFactor = 0.1
        val scaledShapes = shapes.mapValues { (_, shape) ->
            AffineTransform.getScaleInstance(1 / scaleFactor, 1 / scaleFactor).createTransformedShape(shape)
        }

        val bounds = scaledShapes.values.map { it.bounds2D }.reduce { acc, rect -> acc.createUnion(rect) }
        val shapesPanel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g as Graphics2D)
                g.translate(-bounds.minX, -bounds.minY)
                scaledShapes.forEach { (key, shape) ->
                    val ord = key[0] - 'A'
                    g.color = Color(
                        50 * (ord % 5),
                        0,
                        127 * (ord / 5),
                    )
                    g.fill(shape)
                }
            }
        }
        shapesPanel.size = Dimension(bounds.width.roundToInt(), bounds.height.roundToInt())
        RenderTestUtils.compareRendering("HexJsonReader", testMethod, shapesPanel)
    }
}
