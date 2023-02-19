package com.joecollins.graphics.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import javax.swing.JPanel
import kotlin.math.roundToInt

class GeoJsonReaderTest {

    @Test
    fun testReadGeoJson() {
        val shapes = GeoJsonReader.readShapes(
            GeoJsonReaderTest::class.java
                .classLoader
                .getResource("com/joecollins/graphics/geojsons/uk_regions.geojson"),
            "rgn19nm",
            String::class.java,
        )
        Assertions.assertEquals(
            setOf(
                "South West",
                "South East",
                "London",
                "East",
                "East Midlands",
                "West Midlands",
                "Yorkshire and the Humber",
                "North West",
                "North East",
                "Wales",
                "Scotland",
                "Northern Ireland",
            ),
            shapes.keys,
        )

        val scaleFactor = 0.01
        val scaledShapes = shapes.mapValues { (_, shape) ->
            AffineTransform.getScaleInstance(1 / scaleFactor, 1 / scaleFactor).createTransformedShape(shape)
        }

        val bounds = scaledShapes.values.map { it.bounds2D }.reduce { acc, rect -> acc.createUnion(rect) }
        val shapesPanel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g as Graphics2D)
                g.translate(-bounds.minX, -bounds.minY)
                scaledShapes.forEach { (region, shape) ->
                    g.color = Color(region.hashCode())
                    g.fill(shape)
                }
            }
        }
        shapesPanel.size = Dimension(bounds.width.roundToInt(), bounds.height.roundToInt())
        RenderTestUtils.compareRendering("GeoJsonReader", "readGeoJson", shapesPanel)
    }
}
