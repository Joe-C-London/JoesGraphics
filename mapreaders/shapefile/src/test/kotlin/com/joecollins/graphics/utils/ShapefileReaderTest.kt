package com.joecollins.graphics.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.locationtech.jts.awt.ShapeWriter
import org.locationtech.jts.geom.Envelope
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.Polygon
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.util.concurrent.CyclicBarrier
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
        assertEquals((1..27).toSet(), shapes.keys)

        val scaleFactor = 100.0
        val shapeWriter = ShapeWriter()
        val flip = AffineTransform.getScaleInstance(1.0, -1.0)
        val scaledShapes = shapes.mapValues { (_, geom) ->
            val awt = flip.createTransformedShape(shapeWriter.toShape(geom))
            AffineTransform.getScaleInstance(1 / scaleFactor, 1 / scaleFactor).createTransformedShape(awt)
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

    /**
     * JTS caches a geometry's envelope lazily into a non-volatile field, so whichever thread
     * touches it first unsafely publishes it and any concurrent reader can observe the default
     * 0.0 coordinates.  [GenericReader.readShapes] defends against this by computing every
     * envelope on the loading thread, so that they are only ever read afterwards.
     */
    @Test
    fun testEnvelopesAreMaterialisedOnLoad() {
        val shapes = readPeiDistricts()
        val components = shapes.values.flatMap { it.selfAndComponents() }
        val unmaterialised = components.filter { envelopeField.get(it) == null }
        assertEquals(
            0,
            unmaterialised.size,
            "${unmaterialised.size} of ${components.size} geometries still have a lazily-computed " +
                "envelope: ${unmaterialised.groupingBy { it.geometryType }.eachCount()}",
        )
    }

    /**
     * Reading the same freshly-loaded geometries from many threads at once must never surface a
     * torn envelope.  Without the eager computation above this fails intermittently, with
     * envelopes collapsed towards (0, 0).
     */
    @Test
    fun testConcurrentEnvelopeReadsAreConsistent() {
        repeat(20) {
            val shapes = readPeiDistricts()
            val expected = shapes.mapValues { (_, geom) -> Envelope(geom.envelopeInternal) }
            val threadCount = 8
            val barrier = CyclicBarrier(threadCount)
            val observed = List(threadCount) {
                Thread.ofPlatform().start {
                    barrier.await()
                    shapes.forEach { (key, geom) ->
                        assertEquals(expected[key], geom.envelopeInternal, "district $key")
                    }
                }
            }
            observed.forEach { it.join() }
        }
    }

    private fun readPeiDistricts() = ShapefileReader.readShapes(
        ShapefileReaderTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp"),
        "DIST_NO",
        Int::class.java,
    )

    private fun Geometry.selfAndComponents(): List<Geometry> = listOf(this) + when (this) {
        is Polygon -> exteriorRing.selfAndComponents() +
            (0 until numInteriorRing).flatMap { getInteriorRingN(it).selfAndComponents() }

        is GeometryCollection -> (0 until numGeometries).flatMap { getGeometryN(it).selfAndComponents() }

        else -> emptyList()
    }

    companion object {
        private val envelopeField = Geometry::class.java.getDeclaredField("envelope")
            .apply { isAccessible = true }
    }
}
