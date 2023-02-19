package com.joecollins.graphics.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import javax.swing.JPanel
import kotlin.math.roundToInt

class ShapefileReaderTest {

    @Test
    fun testReadShapefile() {
        val shapes = ShapefileReader.readShapes(
            ShapefileReaderTest::class.java
                .classLoader
                .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp"),
            "DIST_NO",
            Int::class.java,
        )
        assertEquals(27, shapes.size)

        val scaleFactor = 100.0
        val scaledShapes = shapes.mapValues { (_, shape) ->
            AffineTransform.getScaleInstance(1 / scaleFactor, 1 / scaleFactor).createTransformedShape(shape)
        }

        val bounds = scaledShapes.values.map { it.bounds2D }.reduce { acc, rect -> acc.createUnion(rect) }
        val shapesPanel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g as Graphics2D)
                g.translate(-bounds.minX, -bounds.minY)
                scaledShapes.forEach { (distNo, shape) ->
                    g.color = Color(
                        50 * (distNo % 5),
                        0,
                        40 * (distNo / 5),
                    )
                    g.fill(shape)
                }
            }
        }
        shapesPanel.size = Dimension(bounds.width.roundToInt(), bounds.height.roundToInt())
        RenderTestUtils.compareRendering("ShapefileReader", "readShapefile", shapesPanel)
    }
}
