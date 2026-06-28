package com.joecollins.graphics.components

import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import java.awt.Color
import java.awt.geom.Rectangle2D

class MapFrameBuilderTest {
    private val gf = GeometryFactory()

    private fun rect(x: Double, y: Double, w: Double, h: Double): Geometry = gf.createPolygon(
        arrayOf(
            Coordinate(x, y),
            Coordinate(x + w, y),
            Coordinate(x + w, y + h),
            Coordinate(x, y + h),
            Coordinate(x, y),
        ),
    )

    private val redShape get() = rect(2.0, 2.0, 1.0, 1.0)
    private val blueShape get() = rect(5.0, 5.0, 2.0, 2.0)

    @Test
    fun testBasicMapFrame() {
        val red = redShape
        val blue = blueShape
        val shapes = Publisher(
            listOf(
                Pair(red, Color.RED),
                Pair(blue, Color.BLUE),
            ),
        )
        val frame = MapFrameBuilder.from(
            shapes = shapes,
            header = "MAP".asOneTimePublisher(),
        )
        assertEquals(2, frame.numShapes)
        assertEquals(red, frame.getShape(0))
        assertEquals(Color.RED, frame.getColor(0))
        assertEquals(blue, frame.getShape(1))
        assertEquals(Color.BLUE, frame.getColor(1))
        assertEquals("MAP", frame.header)
        assertEquals(Rectangle2D.Double(2.0, -7.0, 5.0, 5.0), frame.focusBox)
    }

    @Test
    fun testBasicMapFrameWithListBinding() {
        val red = redShape
        val blue = blueShape
        val shapes = listOf(
            Pair(red, Color.RED),
            Pair(blue, Color.BLUE),
        )
        val frame: MapFrame = MapFrameBuilder.from(
            shapes = shapes.asOneTimePublisher(),
            header = "MAP".asOneTimePublisher(),
        )
        assertEquals(2, frame.numShapes)
        assertEquals(red, frame.getShape(0))
        assertEquals(Color.RED, frame.getColor(0))
        assertEquals(blue, frame.getShape(1))
        assertEquals(Color.BLUE, frame.getColor(1))
        assertEquals("MAP", frame.header)
        assertEquals(Rectangle2D.Double(2.0, -7.0, 5.0, 5.0), frame.focusBox)
    }

    @Test
    fun testMapPropertyBinding() {
        class ConstituencyPair(val shape: Geometry, val color: Color)

        val red = redShape
        val blue = blueShape
        val shapes = listOf(
            ConstituencyPair(red, Color.RED),
            ConstituencyPair(blue, Color.BLUE),
        )
        val frame = MapFrameBuilder.from(
            items = shapes.asOneTimePublisher(),
            shape = { shape },
            color = { color.asOneTimePublisher() },
            header = "MAP".asOneTimePublisher(),
        )
        assertEquals(2, frame.numShapes)
        assertEquals(red, frame.getShape(0))
        assertEquals(Color.RED, frame.getColor(0))
        assertEquals(blue, frame.getShape(1))
        assertEquals(Color.BLUE, frame.getColor(1))
        assertEquals("MAP", frame.header)
        assertEquals(Rectangle2D.Double(2.0, -7.0, 5.0, 5.0), frame.focusBox)
    }

    @Test
    fun testMapItemPropertyBinding() {
        val red = redShape
        val blue = blueShape
        val shapes = listOf(
            Pair(red, Publisher(Color.RED)),
            Pair(blue, Publisher(Color.BLUE)),
        )
        val frame = MapFrameBuilder.from(
            items = shapes.asOneTimePublisher(),
            shape = { first },
            color = { second },
            header = "MAP".asOneTimePublisher(),
        )
        assertEquals(2, frame.numShapes)
        assertEquals(red, frame.getShape(0))
        assertEquals(Color.RED, frame.getColor(0))
        assertEquals(blue, frame.getShape(1))
        assertEquals(Color.BLUE, frame.getColor(1))

        shapes[0].second.submit(Color.GREEN)
        assertEquals(Color.GREEN, frame.getColor(0))
        assertEquals(Color.BLUE, frame.getColor(1))

        shapes[1].second.submit(Color.ORANGE)
        assertEquals(Color.ORANGE, frame.getColor(1))
        assertEquals(Color.GREEN, frame.getColor(0))
    }

    @Test
    fun testFocusBox() {
        val shapes = Publisher(
            listOf(
                Pair(redShape, Color.RED),
                Pair(blueShape, Color.BLUE),
            ),
        )
        val focus = shapes.map { s -> listOf(s[0].first) }
        val frame = MapFrameBuilder.from(
            shapes = shapes,
            header = "MAP".asOneTimePublisher(),
            focus = focus,
        )

        assertEquals(Rectangle2D.Double(2.0, -3.0, 1.0, 1.0), frame.focusBox)
    }

    @Test
    fun testMultiFocusBox() {
        val shapes = Publisher(
            listOf(
                Pair(redShape, Color.RED),
                Pair(blueShape, Color.BLUE),
            ),
        )
        val focus = shapes.mapElements { it.first }
        val frame = MapFrameBuilder.from(
            shapes = shapes,
            header = "MAP".asOneTimePublisher(),
            focus = focus,
        )
        assertEquals(Rectangle2D.Double(2.0, -7.0, 5.0, 5.0), frame.focusBox)
    }

    @Test
    fun testNotes() {
        val shapes = Publisher(
            listOf(
                Pair(redShape, Color.RED),
                Pair(blueShape, Color.BLUE),
            ),
        )
        val frame = MapFrameBuilder.from(
            shapes = shapes,
            header = "MAP".asOneTimePublisher(),
            notes = "A note".asOneTimePublisher(),
        )
        assertEquals("A note", frame.notes)
    }

    @Test
    fun testBorderColor() {
        val shapes = Publisher(
            listOf(
                Pair(redShape, Color.RED),
                Pair(blueShape, Color.BLUE),
            ),
        )
        val frame = MapFrameBuilder.from(
            shapes = shapes,
            header = "MAP".asOneTimePublisher(),
            borderColor = Color.GRAY.asOneTimePublisher(),
        )
        assertEquals(Color.GRAY, frame.borderColor)
    }
}
